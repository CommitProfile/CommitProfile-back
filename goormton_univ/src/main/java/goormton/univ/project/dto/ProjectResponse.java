package goormton.univ.project.dto;

import jdk.jshell.Snippet;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProjectResponse {
    private Long id;      // Project PK
    private Long memberId;

    private String projectName;
    private String teamName;
    private String description;
    private String period;
    private String techStack;
    private String githubUrl;
    private String contributionDescription;



}
