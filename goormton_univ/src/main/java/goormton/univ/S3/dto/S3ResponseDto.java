package goormton.univ.S3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class S3ResponseDto {
    private String message;  // 성공 / 실패 메시지
    private String url;      // 업로드된 이미지 URL (삭제 시 null)
    private String fileName; // 삭제 대상 파일 이름
    private String error;    // 에러 메시지 (성공 시 null)
}