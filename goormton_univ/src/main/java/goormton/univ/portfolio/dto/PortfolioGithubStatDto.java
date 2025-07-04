package goormton.univ.portfolio.dto;

import goormton.univ.portfolio.entity.GithubStatField;
import lombok.Getter;

import java.util.Set;

@Getter
public class PortfolioGithubStatDto {
    private Set<GithubStatField> includedFields;
    private Integer commitCount;
    private Integer prCount;
    private Integer mergeCount;
    private Integer contributionRatio; // 예: 70이면 70%
}
