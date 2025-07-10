package goormton.univ.project.domain;

import goormton.univ.member.entity.Member;
import goormton.univ.project.dto.ProjectRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName; //프로젝트 이름
    private String teamName; //팀 이름
    private String description; //기획 의도 및 소개
    private LocalDate startDate; // 프로젝트 시작일
    private LocalDate endDate; // 프로젝트 종료일

    @ElementCollection
    private List<String> features; // 주요 기능 및 특징
    @ElementCollection
    private List<String> contributionDescriptions; // 작업 기여도

    private String serviceLogoPath; // 업로드된 파일 경로 (예: "/uploads/logos/logo1.png")


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void updateFrom(ProjectRequest request, Member member, String serviceLogoPath) {
        this.projectName = request.getProjectName();
        this.teamName = request.getTeamName();
        this.description = request.getDescription();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.features = request.getFeatures();
        this.contributionDescriptions = request.getContributionDescriptions();
        this.serviceLogoPath = serviceLogoPath;
        this.member = member;
    }
}