package goormton.univ.member.controller;

import goormton.univ.member.service.GithubAuthService;
import goormton.univ.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class GithubLoginController {

    private final GithubAuthService githubAuthService;

    /**
     * GitHub 로그인 페이지로 리디렉션
     */
    @GetMapping("/login/github")
    public ResponseEntity<String> redirectToGithubLogin() {
        String githubLoginUrl = githubAuthService.getGithubAuthUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(githubLoginUrl));
        return ResponseEntity.status(302).headers(headers).build();
    }

    /**
     * GitHub 로그인 콜백 처리
     */
    @GetMapping("/github/callback")
    public ResponseEntity<?> handleGithubCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state) {
        try {
            // state가 있으면 계정 연동, 없으면 일반 로그인
            if (state != null && !state.trim().isEmpty()) {
                // 계정 연동 처리
                Map<String, Object> linkResult = githubAuthService.processGithubLinkCallback(code, state);
                return ResponseEntity.ok(linkResult);
            } else {
                // 일반 로그인 처리
                Map<String, Object> authResult = githubAuthService.processGithubLoginCallback(code);
                return ResponseEntity.ok(authResult);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", state != null ? "GitHub 계정 연동 중 오류가 발생했습니다." : "GitHub OAuth 처리 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/link/github/{memberId}")
    public ResponseEntity<String> startGithubLink(@PathVariable Long memberId) {

        String githubLinkUrl = githubAuthService.getGithubLinkUrl(memberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(githubLinkUrl));
        return ResponseEntity.status(302).headers(headers).build();
    }


    @DeleteMapping("/unlink/github")
    public ResponseEntity<Map<String, Object>> unlinkGithubAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        try {
            // GitHub 계정 연결 해제
            githubAuthService.unlinkGithubAccount(memberId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "GitHub 계정이 성공적으로 연결 해제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "GitHub 계정 연결 해제 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/github/user")
    public ResponseEntity<Map<String, Object>> getGithubUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        try {
            // GitHub 사용자 정보 조회
            Map<String, Object> userInfo = githubAuthService.getGithubLinkStatus(memberId);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "GitHub 사용자 정보 조회 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
