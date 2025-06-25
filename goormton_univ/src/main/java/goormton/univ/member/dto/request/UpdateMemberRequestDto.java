package goormton.univ.member.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequestDto {

    private String email;

    private String password;

    private String nickName;

    private String name;

    private String profileImageUrl;
}
