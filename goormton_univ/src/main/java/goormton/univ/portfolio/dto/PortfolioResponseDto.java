package goormton.univ.portfolio.dto;

import goormton.univ.portfolio.entity.Portfolio;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PortfolioResponseDto {
    private Long portfolioId;
    private String userInfoJson;
    private String projectsJson;
    private LocalDateTime createdAt;

    public static PortfolioResponseDto fromEntity(Portfolio portfolio) {
        PortfolioResponseDto dto = new PortfolioResponseDto();
        dto.portfolioId = portfolio.getPortfolioId();
        dto.userInfoJson = portfolio.getUserInfoJson();
        dto.projectsJson = portfolio.getProjectsJson();
        dto.createdAt = portfolio.getCreatedAt();
        return dto;
    }
}
