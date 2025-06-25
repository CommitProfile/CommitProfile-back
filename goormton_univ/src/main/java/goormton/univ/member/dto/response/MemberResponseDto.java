package goormton.univ.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor

public class MemberResponseDto {
    private Long id;
    private String name;
    private String nickName;
    private String profileImage;
    private List<PetDto> pets;

    @Getter
    @AllArgsConstructor
    public static class PetDto {
        private Long id;
        private String name;
        private String type;
        private LocalDate birthDate;
        private String petDetail;
        private String petImg;
    }
}