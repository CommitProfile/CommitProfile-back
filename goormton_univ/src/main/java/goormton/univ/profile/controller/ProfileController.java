package goormton.univ.profile.controller;

import goormton.univ.profile.dto.CreateProfileRequestDto;
import goormton.univ.profile.dto.ProfileResponseDto;
import goormton.univ.profile.dto.UpdateProfileRequestDto;
import goormton.univ.profile.service.ProfileService;
import goormton.univ.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponseDto> createProfile(@Valid @RequestBody CreateProfileRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        ProfileResponseDto responseDto = profileService.createProfile(requestDto, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(@Valid @RequestBody UpdateProfileRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponseDto responseDto = profileService.updateProfile(requestDto, userDetails.getMember().getId());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        profileService.deleteProfile(userDetails.getMember().getId());
        return ResponseEntity.ok("프로필이 성공적으로 삭제되었습니다.");
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable Long memberId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponseDto responseDto = profileService.getProfile(memberId, userDetails.getMember().getId());
        return ResponseEntity.ok(responseDto);
    }


}
