package goormton.univ.portfolio.entity;

import goormton.univ.member.entity.Member;
import goormton.univ.portfolio.dto.PortfolioCreateDto;
import goormton.univ.portfolio.dto.PortfolioUpdateDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(columnDefinition = "json")
    private String userInfoJson;

    @Column(columnDefinition = "json")
    private String projectsJson;

    @Column(columnDefinition = "json")
    private String githubStatsJson;

    @Column(columnDefinition = "json")
    private String commitMessagesJson;

    private LocalDateTime createdAt;

    public void initializePortfolio(Member member, LocalDateTime createdAt) {
        this.member = member;
        this.createdAt = createdAt;
    }
    public void saveUserInfoJson(String userInfoJson){
        this.userInfoJson = userInfoJson;
    }

    public void saveProjectsJson(String projectsJson){
        this.projectsJson = projectsJson;
    }

    public void saveGithubStatsJson(String githubStatsJson){
        this.githubStatsJson = githubStatsJson;
    }

    public void saveCommitMessagesJson(String commitMessagesJson){
        this.commitMessagesJson = commitMessagesJson;
    }


}
