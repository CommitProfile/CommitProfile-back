package goormton.univ.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProjectRequest {

    private Long id;
    private Long memberId;

    private String projectName;         // 프로젝트 이름
    private String teamName;            // 팀 이름
    private String description;         // 기획 의도 및 소개

    private LocalDate startDate;        // 프로젝트 시작일
    private LocalDate endDate;          // 프로젝트 종료일

    private List<String> features;      // 주요 기능 및 특징
    private List<String> contributionDescriptions; // 작업 기여도
}
