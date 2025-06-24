package goormton.univ.portfolio.service;

import goormton.univ.portfolio.dto.PortfolioCreateDto;
import goormton.univ.portfolio.dto.PortfolioResponseDto;
import goormton.univ.portfolio.dto.PortfolioUpdateDto;
import org.springframework.stereotype.Service;

public interface PortfolioService {
    PortfolioResponseDto findById(Long id);
    void update(Long id, PortfolioUpdateDto request);
    void delete(Long id);
    Long create(PortfolioCreateDto portfolioCreateDto);

}