package goormton.univ.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProjectRequest {

    private Long id;
    private Long userId;
    private String projectName;
    private String teamName;
    private String description;
    private String period;
    private String techStack;
    private String githubUrl;
    private String contributionDescription;



}
