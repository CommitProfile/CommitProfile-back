package goormton.univ.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.member.entity.Member;
import goormton.univ.member.repository.MemberRepository;
import goormton.univ.security.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class GithubAuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectUri;

    private static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";

    public String getGithubAuthUrl() {
        return "https://github.com/login/oauth/authorize" + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=user:email";
    }

    public String getGithubLinkUrl(Long memberId) {
        String state = generateSecureState(memberId);

        return "https://github.com/login/oauth/authorize" + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=user:email"
                + "&state=" + state;
    }

    // 보안을 위한 State 생성
    private String generateSecureState(Long memberId) {

        return Jwts.builder()
                .setSubject(memberId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public Map<String, Object> processGithubLoginCallback(String code) {
        return processGithubCallback(code);
    }

    public Map<String, Object> processGithubLinkCallback(String code, String state) {
        Long memberId = validateStateAndExtractMemberId(state);
        Map<String, Object> linkResult = linkGithubToExistingAccount(memberId, code);

        return linkResult;
    }
    // State 검증 및 memberId 추출
    public Long validateStateAndExtractMemberId(String state) {
        try {
            return Long.parseLong(Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(state)
                    .getBody()
                    .getSubject());
        } catch (Exception e) {
            log.error("State 검증 실패: {}", e.getMessage());
            throw new RuntimeException("유효하지 않은 요청입니다.");
        }
    }

    public Map<String, Object> processGithubCallback(String code) {
        // Access Token 획득
        String accessToken = getAccessToken(code);
        // Github 사용자 정보 획득
        Map<String, Object> githubUserInfo = getUserInfo(accessToken);
        // 회원 처리 (회원가입 또는 기존 회원 로그인)
        Member member = processUserRegistration(githubUserInfo);
        // JWT 토큰 생성
        String jwtToken = jwtUtil.generateIdToken(member.getId());
        String refreshToken = memberService.generateOrUpdateRefreshToken(member);
        // 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessToken", jwtToken);
        result.put("refreshToken", refreshToken);
        result.put("member", createUserResponse(member));

        return result;
    }

    private String getAccessToken(String code){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("client_id", clientId);
        tokenRequest.put("client_secret", clientSecret);
        tokenRequest.put("code", code);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(tokenRequest, headers);

        try {
            //Github에 Access Token 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    ACCESS_TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            //응답에서 access_token 추출
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            String accessToken = (String) responseBody.get("access_token");
            if(accessToken == null){
                log.error("Github Access Token 요청 실패: {}", response.getBody());
                throw new RuntimeException("Github에서 Access Token을 받을 수 없습니다.");
            }

            log.info("Github Access Token 획득 성공: {}", accessToken);
            return accessToken;
        } catch (Exception e) {
            log.error("Github Access Token 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Github Access Token 요청 실패");
        }
    }

    // Github에서 사용자 정보 획득
    private Map<String, Object> getUserInfo(String accessToken) {
        //요청에서 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            //JSON 응답을 Map으로 변환
            Map<String, Object> userInfo = objectMapper.readValue(response.getBody(), Map.class);

            log.info("Github 사용자 정보 획득 성공: {}", userInfo);
            return userInfo;
        } catch (Exception e) {
            log.error("Github 사용자 정보 요청 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Github 사용자 정보 요청 실패");
        }
    }

    private Member processUserRegistration(Map<String, Object> githubUserInfo) {
        // Github 정보 추출
        String githubId = String.valueOf(githubUserInfo.get("id"));
        String githubLogin = (String) githubUserInfo.get("login");
        String name = (String) githubUserInfo.get("name");
        String email = (String) githubUserInfo.get("email");
        String profileImageUrl = (String) githubUserInfo.get("avatar_url");

        if(email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Github 계정의 이메일 정보가 필요합니다. Github 설정에서 이메일을 공개로 변경해주세요.");
        }

        //기존 Github 사용자 검색
        Optional<Member> existingGithubMember = memberRepository.findByGithubId(githubId);
        if(existingGithubMember.isPresent()){
            log.info("기존 Github 회원 정보가 존재합니다. ID: {}", githubLogin);
            return existingGithubMember.get();
        }

        // 동일한 이메일로 가입된 일반 회원이 있는지 확인
        Optional<Member> existingEmailMember = memberRepository.findByEmail(email);
        if (existingEmailMember.isPresent()) {
            Member member = existingEmailMember.get();
            member.setGithubId(githubId);
            member.setGithubLogin(githubLogin);

            // 프로필 이미지가 없거나 기본 이미지인 경우에만 업데이트
            if(profileImageUrl != null && (member.getProfileImageUrl() == null || member.getProfileImageUrl().contains("default"))){
                member.setProfileImageUrl(profileImageUrl);
            }

            log.info("기존 일반 회원에게 Github 계정 연동: {}", email);
            return memberRepository.save(member);
        }

        String nickName = githubLogin;
        if(memberRepository.existsByNickName(nickName)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickName);
        }

        Member newMember = Member.createGithubMember(
                email,
                nickName,
                name != null ? name : githubLogin,
                profileImageUrl != null ? profileImageUrl : "https://avatars.githubusercontent.com/u/0?v=4",
                githubId,
                githubLogin
        );

        newMember.setCreatedAt(LocalDateTime.now());

        Member savedMember = memberRepository.save(newMember);
        log.info("새로운 Github 회원 가입 성공: {}", savedMember.getEmail());
        return savedMember;

    }

    // 일반 사용자 깃헙 연동하기
    public Map<String, Object> linkGithubToExistingAccount(Long memberId, String code) {
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 이미 Github 연동된 계정인지 확인
        if(currentMember.getGithubId() != null) {
            throw new RuntimeException("이미 Github 계정이 연동되어 있습니다.");
        }

        // Github OAuth 처리
        String accessToken = getAccessToken(code);
        Map<String, Object> githubUserInfo = getUserInfo(accessToken);

        String githubId = String.valueOf(githubUserInfo.get("id"));
        String githubLogin = (String) githubUserInfo.get("login");
        String profileImageUrl = (String) githubUserInfo.get("avatar_url");

        // 다른 계정에서 이미 사용 중인 Github ID인지 확인
        Optional<Member> existingGithubMember = memberRepository.findByGithubId(githubId);
        if (existingGithubMember.isPresent()) {
            throw new RuntimeException("이미 다른 계정에 연동된 Github 계정입니다.");
        }

        currentMember.setGithubId(githubId);
        currentMember.setGithubLogin(githubLogin);

        if (profileImageUrl != null &&
            (currentMember.getProfileImageUrl() == null || currentMember.getProfileImageUrl().contains("default"))) {
            currentMember.setProfileImageUrl(profileImageUrl);
        }

        memberRepository.save(currentMember);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Github 계정이 성공적으로 연동되었습니다.");
        response.put("githubLogin", githubLogin);
        return response;
    }

    // 깃헙 연동 해제하기
    public void unlinkGithubAccount(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        if (member.getGithubId() == null) {
            throw new RuntimeException("연동된 Github 계정이 없습니다.");
        }

        member.setGithubId(null);
        member.setGithubLogin(null);
        memberRepository.save(member);
    }

    // 깃헙 링크 정보 가져오기
    public Map<String, Object> getGithubLinkStatus(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        if (member.getGithubId() == null) {
            throw new RuntimeException("연동된 Github 계정이 없습니다.");
        }

        Map<String, Object> status = createUserResponse(member);
        status.put("success", true);
        status.put("githubId", member.getGithubId());
        status.put("githubLogin", member.getGithubLogin());

        return status;
    }

    private Map<String, Object> createUserResponse(Member member) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", member.getId());
        userResponse.put("email", member.getEmail());
        userResponse.put("nickName", member.getNickName());
        userResponse.put("name", member.getName());
        userResponse.put("profileImageUrl", member.getProfileImageUrl());
        userResponse.put("githubId", member.getGithubId());
        userResponse.put("githubLogin", member.getGithubLogin());
        return userResponse;
    }
}