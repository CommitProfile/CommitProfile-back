package goormton.univ.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProfileRequestDto {
    private String introduction;
    private String phoneNumber;
    private String skills;
    private String githubUrl;
    private String blogUrl;

    public static CreateProfileRequestDto fromEntity(String introduction, String phoneNumber, String skills, String githubUrl, String blogUrl) {
        CreateProfileRequestDto dto = new CreateProfileRequestDto();
        dto.setIntroduction(introduction);
        dto.setPhoneNumber(phoneNumber);
        dto.setSkills(skills);
        dto.setGithubUrl(githubUrl);
        dto.setBlogUrl(blogUrl);
        return dto;
    }
}
