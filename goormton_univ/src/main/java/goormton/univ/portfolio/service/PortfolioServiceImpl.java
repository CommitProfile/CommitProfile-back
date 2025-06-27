package goormton.univ.portfolio.service;

import goormton.univ.portfolio.dto.PortfolioCreateDto;
import goormton.univ.portfolio.dto.PortfolioResponseDto;
import goormton.univ.portfolio.dto.PortfolioUpdateDto;
import goormton.univ.portfolio.entity.Portfolio;
import goormton.univ.portfolio.repository.ProtfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final ProtfolioRepository portfolioRepository;

    @Override
    public PortfolioResponseDto findById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("포트폴리오가 존재하지 않습니다."));
        return PortfolioResponseDto.fromEntity(portfolio);
    }

    @Override
    public void update(Long id, PortfolioUpdateDto portfolioUpdateDto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("포트폴리오가 존재하지 않습니다."));
        portfolio.updatePortfolio(portfolioUpdateDto);
    }

    @Override
    public void delete(Long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
    }

    @Override
    public Long create(PortfolioCreateDto portfolioCreateDto) {
        Portfolio portfolio = Portfolio.from(portfolioCreateDto);
        return portfolioRepository.save(portfolio).getPortfolioId();
    }
}