package goormton.univ.portfolio.dto;

import goormton.univ.portfolio.entity.ProjectField;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class PortfolioProjectDto {
    private Set<ProjectField> includedFields;
    private String name;
    private String team;
    private String description;
    private String feature;
    private String contribution;
    private LocalDateTime startdate;
    private LocalDateTime enddate;

    // 서비스 로고는 따로 받아옴
}
