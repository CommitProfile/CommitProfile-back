package goormton.univ.portfolio.dto;

import goormton.univ.portfolio.entity.Portfolio;
import lombok.Getter;

@Getter
public class PortfolioResponseDto {
    private Long portfolioId;
    private String portfolioText;

    public static PortfolioResponseDto fromEntity(Portfolio portfolio) {
        PortfolioResponseDto portfolioResponseDto = new PortfolioResponseDto();
        portfolioResponseDto.portfolioId = portfolio.getPortfolioId();
        portfolioResponseDto.portfolioText = portfolio.getPortfolioText();
        return portfolioResponseDto;
    }
}
