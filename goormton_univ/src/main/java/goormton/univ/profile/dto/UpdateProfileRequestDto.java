package goormton.univ.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequestDto {
    private String introduction;
    private String phoneNumber;
    private String skills;
    private String githubUrl;
    private String blogUrl;
}
