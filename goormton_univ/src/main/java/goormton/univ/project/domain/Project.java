package goormton.univ.project.domain;

import goormton.univ.member.entity.Member;
import goormton.univ.project.dto.ProjectRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String teamName;
    private String description;
    private String period;
    private String techStack;
    private String githubUrl;
    private String contributionDescription; //작업기여도


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")  // FK
    private Member member;

    public void updateFrom(ProjectRequest request, Member member) {
        this.projectName = request.getProjectName();
        this.teamName = request.getTeamName();
        this.description = request.getDescription();
        this.period = request.getPeriod();
        this.techStack = request.getTechStack();
        this.githubUrl = request.getGithubUrl();
        this.contributionDescription = request.getContributionDescription();
        this.member = member;
    }



}
