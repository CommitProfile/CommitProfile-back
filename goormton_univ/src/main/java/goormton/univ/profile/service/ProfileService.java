package goormton.univ.profile.service;

import goormton.univ.member.entity.Member;
import goormton.univ.member.repository.MemberRepository;
import goormton.univ.profile.dto.CreateProfileRequestDto;
import goormton.univ.profile.dto.ProfileResponseDto;
import goormton.univ.profile.dto.CreateProfileRequestDto;
import goormton.univ.profile.dto.UpdateProfileRequestDto;
import goormton.univ.profile.entity.Profile;
import goormton.univ.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;

    public ProfileResponseDto createProfile(CreateProfileRequestDto requestDto, Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Profile profile = new Profile();
        profile.setIntroduction(requestDto.getIntroduction());
        profile.setPhoneNumber(requestDto.getPhoneNumber());
        profile.setSkills(requestDto.getSkills());
        profile.setGithubUrl(requestDto.getGithubUrl());
        profile.setBlogUrl(requestDto.getBlogUrl());

        profile.setMember(member);
        member.setProfile(profile);
        profileRepository.save(profile);
        return ProfileResponseDto.fromEntity(profile); // 반환할 DTO 객체 생성
    }

    public ProfileResponseDto updateProfile(UpdateProfileRequestDto requestDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Profile profile = profileRepository.findByMemberId(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        // 요청에서 null이 아닌 값만 업데이트 (기존 값은 유지)
        if (requestDto.getIntroduction() != null) {
            profile.setIntroduction(requestDto.getIntroduction());
        }

        if (requestDto.getPhoneNumber() != null) {
            profile.setPhoneNumber(requestDto.getPhoneNumber());
        }

        if (requestDto.getSkills() != null) {
            profile.setSkills(requestDto.getSkills());
        }

        if (requestDto.getGithubUrl() != null) {
            profile.setGithubUrl(requestDto.getGithubUrl());
        }

        if (requestDto.getBlogUrl() != null) {
            profile.setBlogUrl(requestDto.getBlogUrl());
        }

        profileRepository.save(profile);

        return ProfileResponseDto.fromEntity(profile);
    }

    public void deleteProfile(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Profile profile = profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        profileRepository.delete(profile);
    }

    public ProfileResponseDto getProfile(Long memberId, Long requestMemberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Profile profile = profileRepository.findByMemberId(requestMemberId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        return ProfileResponseDto.fromEntity(profile);
    }

}
