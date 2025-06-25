package goormton.univ.member.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequestDto {

    private String nickName;

    private String name;

    private String profileImageUrl;
}
