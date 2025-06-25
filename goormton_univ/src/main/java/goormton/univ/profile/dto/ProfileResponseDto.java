package goormton.univ.profile.dto;

import goormton.univ.profile.entity.Profile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponseDto {
    private String introduction;
    private String phoneNumber;
    private String skills;
    private String githubUrl;
    private String blogUrl;

    public static ProfileResponseDto fromEntity(Profile profile) {
        ProfileResponseDto responseDto = new ProfileResponseDto();
        responseDto.setIntroduction(profile.getIntroduction());
        responseDto.setPhoneNumber(profile.getPhoneNumber());
        responseDto.setSkills(profile.getSkills());
        responseDto.setGithubUrl(profile.getGithubUrl());
        responseDto.setBlogUrl(profile.getBlogUrl());
        return responseDto;
    }
}


