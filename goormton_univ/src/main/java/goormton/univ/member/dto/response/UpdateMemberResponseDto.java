package goormton.univ.member.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberResponseDto {

    private Long id;
    private String email;
    private String nickName;
    private String name;
    private String profileImageUrl;

    public UpdateMemberResponseDto(Long id, String email, String nickName, String name, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.nickName = nickName;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
}