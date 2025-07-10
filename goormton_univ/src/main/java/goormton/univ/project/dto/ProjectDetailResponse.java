package goormton.univ.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetailResponse {

    private Long id;
    private String projectName;      // 프로젝트 이름
    private String teamName;         // 팀 이름
    private String description;      // 기획 의도 및 소개

    private LocalDate startDate;
    private LocalDate endDate;

    private List<String> features;   // 주요 기능
    private List<String> contributionDescriptions; // 작업 기여도
    private String serviceLogoPath;  // 로고 이미지 경로

}
