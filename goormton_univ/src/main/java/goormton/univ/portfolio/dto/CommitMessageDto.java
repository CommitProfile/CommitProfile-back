package goormton.univ.portfolio.dto;

import lombok.Getter;

@Getter
public class CommitMessageDto {
    private boolean included;       // 포함 여부
    private String message;         // 커밋 메시지 내용
}
