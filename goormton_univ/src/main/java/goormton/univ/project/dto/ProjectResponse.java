package goormton.univ.project.dto;

import jdk.jshell.Snippet;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProjectResponse {
    private Long id;                         // Project PK
    private Long userId;

    private String projectName;
    private String teamName;
    private String description;
    private String period;
    private String techStack;
    private String githubUrl;
    private String contributionDescription;



}
