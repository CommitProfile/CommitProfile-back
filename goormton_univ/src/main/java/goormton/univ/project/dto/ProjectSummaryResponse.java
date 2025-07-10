package goormton.univ.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSummaryResponse {
    private Long id;
    private String projectName;
    private String teamName;
    private String period; // ← 여기에 위의 함수로 만들어진 값이 들어감
    private String description;
    private String serviceLogoPath;
    private List<String> contributionDescriptions;
}
