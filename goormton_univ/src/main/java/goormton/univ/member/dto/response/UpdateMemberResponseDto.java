package goormton.univ.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateMemberResponseDto {

    private Long id;
    private String email;
    private String nickName;
    private String name;
    private String profileImageUrl;
}