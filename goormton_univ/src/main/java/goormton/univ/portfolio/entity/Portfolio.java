package goormton.univ.portfolio.entity;

import goormton.univ.portfolio.dto.PortfolioCreateDto;
import goormton.univ.portfolio.dto.PortfolioUpdateDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {
    @Id
    @GeneratedValue
    private Long portfolioId;

    private String portfolioText;

    @Lob
    private String contentJson;

    private LocalDateTime createdAt;

    public static Portfolio from(PortfolioCreateDto dto) {
        return Portfolio.builder()
                .portfolioText(dto.getPortfolioText())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updatePortfolio(PortfolioUpdateDto portfolioUpdateDto) {
        this.portfolioText = portfolioUpdateDto.getPortfolioText();
    }
}
