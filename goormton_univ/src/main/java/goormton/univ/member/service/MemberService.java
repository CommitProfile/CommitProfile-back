package goormton.univ.member.service;

import goormton.univ.S3.S3UploadUtil;
import goormton.univ.S3.service.S3AsyncService;
import goormton.univ.member.dto.request.LoginRequestDto;
import goormton.univ.member.dto.request.SignUpRequestDto;
import goormton.univ.member.dto.request.UpdateMemberRequestDto;
import goormton.univ.member.dto.response.MemberResponseDto;
import goormton.univ.member.dto.response.UpdateMemberResponseDto;
import goormton.univ.member.entity.Member;
import goormton.univ.member.entity.RefreshToken;
import goormton.univ.member.repository.MemberRepository;
import goormton.univ.member.repository.RefreshTokenRepository;
import goormton.univ.security.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3AsyncService s3AsyncService;
    private final S3UploadUtil s3UploadUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 회원가입
     */
    @Transactional
    public CompletableFuture<Void> registerUser(SignUpRequestDto request, MultipartFile profileImage) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (memberRepository.existsByNickName(request.getNickName())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 프로필 이미지 업로드 (비동기 처리)
        String pathPrefix = "profile_images/" + request.getNickName();
        String defaultImageUrl = "";
        CompletableFuture<String> profileImageUrlFuture = s3UploadUtil.uploadImageAsync(profileImage, pathPrefix, defaultImageUrl);

        // 업로드 완료 후 Member 저장
        return profileImageUrlFuture.thenAccept(profileImageUrl -> {
            Member member = new Member(request.getEmail(), encodedPassword, request.getNickName(), profileImageUrl, request.getName());
            member.setCreatedAt(LocalDateTime.now());
            memberRepository.save(member);
        });
    }

    /**
     * 로그인
     */
    public Map<String, String> login(LoginRequestDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        String accessToken = jwtUtil.generateIdToken(member.getId());
        String refreshToken = generateOrUpdateRefreshToken(member);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    public String generateOrUpdateRefreshToken(Member member) {
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByMember(member);
        String refreshToken;

        if (existingTokenOpt.isPresent()) {
            RefreshToken existingToken = existingTokenOpt.get();
            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), existingToken.getExpiryDate());

            if (remainingDays <= 1) {
                refreshToken = jwtUtil.generateRefreshToken();
                existingToken.setToken(refreshToken);
                existingToken.setExpiryDate(jwtUtil.getRefreshTokenExpiryDate());
                refreshTokenRepository.save(existingToken);
            } else {
                refreshToken = existingToken.getToken();
            }
        } else {
            refreshToken = jwtUtil.generateRefreshToken();
            RefreshToken newToken = RefreshToken.builder()
                    .member(member)
                    .token(refreshToken)
                    .expiryDate(jwtUtil.getRefreshTokenExpiryDate())
                    .build();
            refreshTokenRepository.save(newToken);
        }

        return refreshToken;
    }

    /**
     * ID로 사용자 조회
     */
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보 없음"));
    }

    /**
     * 회원 정보 수정
     */
    public CompletableFuture<UpdateMemberResponseDto> updateMember(Long memberId, UpdateMemberRequestDto request, MultipartFile newProfileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 닉네임 변경
        if (request.getNickName() != null && !request.getNickName().isBlank() &&
                !request.getNickName().equals(member.getNickName())) {
            if (memberRepository.existsByNickName(request.getNickName())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            member.setNickName(request.getNickName());
        }

        // 이름 변경
        if (request.getName() != null && !request.getName().isBlank()) {
            member.setName(request.getName());
        }

        String pathPrefix = "profile_images/" + member.getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-profile.png";

        if (newProfileImage == null || newProfileImage.isEmpty()) {
            return CompletableFuture.completedFuture(new UpdateMemberResponseDto(
                    member.getId(),
                    member.getEmail(),
                    member.getNickName(),
                    member.getName(),
                    member.getProfileImageUrl()
            ));
        }

        String oldProfileImageUrl = member.getProfileImageUrl();

        // 새로운 프로필 이미지 업로드
        CompletableFuture<String> profileImageUrlFuture = s3UploadUtil.uploadImageAsync(newProfileImage, pathPrefix, defaultImageUrl);

        return profileImageUrlFuture.thenCompose(newProfileImageUrl -> {
            member.setProfileImageUrl(newProfileImageUrl);
            memberRepository.save(member);

            // 기존 프로필 이미지 삭제
            if (oldProfileImageUrl != null && !oldProfileImageUrl.equals(defaultImageUrl)) {
                String oldFileName = extractFileName(oldProfileImageUrl);
                return s3AsyncService.deleteFile("profile_images/" + oldFileName)
                        .exceptionally(ex -> {
                            System.err.println("S3 파일 삭제 실패: " + ex.getMessage());
                            return null;
                        })
                        .thenApply(ignored -> new UpdateMemberResponseDto(
                                member.getId(),
                                member.getEmail(),
                                member.getNickName(),
                                member.getName(),
                                member.getProfileImageUrl()
                        ));
            }

            return CompletableFuture.completedFuture(new UpdateMemberResponseDto(
                    member.getId(),
                    member.getEmail(),
                    member.getNickName(),
                    member.getName(),
                    member.getProfileImageUrl()
            ));
        });
    }

    public String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        if (imageUrl.contains("/profile_images/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/profile_images/") + "/profile_images/".length());
        }

        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        memberRepository.delete(member);
    }

//    /**
//     * 카카오 로그인 회원 처리
//     */
//    @Transactional
//    public Member handleKakaoLogin(KakaoUserDto kakaoUser) {
//        Optional<Member> existingMember = memberRepository.findByEmail(kakaoUser.getEmail());
//
//        if (existingMember.isPresent()) {
//            return existingMember.get();
//        } else {
//            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
//
//            Member newMember = new Member(
//                    kakaoUser.getEmail(),
//                    randomPassword,
//                    kakaoUser.getNickname(),
//                    kakaoUser.getProfileImageUrl(),
//                    kakaoUser.getNickname()
//            );
//            newMember.setCreatedAt(LocalDateTime.now());
//            memberRepository.save(newMember);
//            return newMember;
//        }
//    }
//
//    /**
//     * 네이버 로그인 회원 처리
//     */
//    @Transactional
//    public Member handleNaverLogin(NaverUserDto naverUser) {
//        Optional<Member> existingMember = memberRepository.findByEmail(naverUser.getEmail());
//
//        if (existingMember.isPresent()) {
//            return existingMember.get();
//        } else {
//            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
//
//            Member newMember = new Member(
//                    naverUser.getEmail(),
//                    randomPassword,
//                    naverUser.getName(),
//                    naverUser.getProfileImage(),
//                    naverUser.getName()
//            );
//            newMember.setCreatedAt(LocalDateTime.now());
//            memberRepository.save(newMember);
//            return newMember;
//        }
//    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long memberId, String refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("이미 만료되었거나 존재하지 않는 토큰입니다."));

        if (!savedToken.getMember().getId().equals(memberId)) {
            throw new SecurityException("토큰의 사용자 정보가 일치하지 않습니다.");
        }

        refreshTokenRepository.delete(savedToken);
    }


    @Transactional
    public Map<String, String> reissueAccessToken(String refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken);
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인 해주세요.");
        }

        Member member = savedToken.getMember();
        String newAccessToken = jwtUtil.generateIdToken(member.getId());

        long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), savedToken.getExpiryDate());

        if (remainingDays <= 1) {
            // RefreshToken 재발급
            String newRefreshToken = jwtUtil.generateRefreshToken();
            savedToken.setToken(newRefreshToken);
            savedToken.setExpiryDate(jwtUtil.getRefreshTokenExpiryDate());
            refreshTokenRepository.save(savedToken);

            return Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            );
        } else {
            return Map.of(
                    "accessToken", newAccessToken
            );
        }
    }
}
