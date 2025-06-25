package goormton.univ.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.member.dto.request.LoginRequestDto;
import goormton.univ.member.dto.request.SignUpRequestDto;
import goormton.univ.member.dto.request.UpdateMemberRequestDto;
import goormton.univ.member.dto.response.UpdateMemberResponseDto;
import goormton.univ.member.entity.Member;
import goormton.univ.member.service.MemberService;
import goormton.univ.security.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    /**
     * 회원 가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<String>> registerUser(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart("userData") String userDataJson) {

        // JSON 데이터를 DTO로 변환
        SignUpRequestDto request;
        try {
            request = objectMapper.readValue(userDataJson, SignUpRequestDto.class);
        } catch (JsonProcessingException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("Invalid JSON format"));
        }

        // 비동기 회원가입 처리 후 응답 반환
        return memberService.registerUser(request, profileImage)
                .thenApply(unused -> ResponseEntity.ok("회원가입 성공"));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, String> tokenMap = memberService.login(request);
        return ResponseEntity.ok(tokenMap);
    }

    /**
     * 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentUser(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(token);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Member member = memberService.findById(memberId);
        return ResponseEntity.ok(member);
    }

    /**
     * 사용자 정보 수정
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<UpdateMemberResponseDto>> updateMember(
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "userData", required = false) String userDataJson) {

        token = token.replace("Bearer ", "");
        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(token);
        } catch (JwtException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        try {
            UpdateMemberRequestDto request = (userDataJson == null || userDataJson.isBlank())
                    ? new UpdateMemberRequestDto()
                    : objectMapper.readValue(userDataJson, UpdateMemberRequestDto.class);

            return memberService.updateMember(memberId, request, profileImage)
                    .thenApply(ResponseEntity::ok);
        } catch (JsonProcessingException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(null));
        }
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(token);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        memberService.deleteMember(memberId);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken,
                                         @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        accessToken = accessToken.replace("Bearer ", "");
        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(accessToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        memberService.logout(memberId, refreshToken);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokenMap = memberService.reissueAccessToken(refreshToken);
        return ResponseEntity.ok(tokenMap);
    }
}