package goormton.univ.portfolio.service;

import goormton.univ.portfolio.dto.*;
import org.springframework.stereotype.Service;

public interface PortfolioService {
    Long saveUserInfo(PortfolioAboutMeDto dto);
    Long saveProjectInfo(Long portfolioId, PortfolioProjectListDto portfolioProjectListDto);
    PortfolioResponseDto findById(Long id);
    Long saveGithubStats(Long portfolioId, PortfolioGithubStatDto portfolioGithubStatDto);
    void delete(Long id);
    Long saveCommitMessages(Long portfolioId, PortfolioCommitMessageDto dto);
}