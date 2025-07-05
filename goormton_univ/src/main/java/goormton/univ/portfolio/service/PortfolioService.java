package goormton.univ.portfolio.service;

import goormton.univ.portfolio.dto.*;
import goormton.univ.portfolio.entity.Portfolio;
import org.springframework.stereotype.Service;

public interface PortfolioService {
    void validateOwnership(Long memberId, Portfolio portfolio);
    Long saveUserInfo(Long memberId, PortfolioAboutMeDto dto);
    Long saveProjectInfo(Long memberId, Long portfolioId, PortfolioProjectListDto portfolioProjectListDto);
    PortfolioResponseDto findById(Long memberId, Long portfolioId);
    Long saveGithubStats(Long memberId, Long portfolioId, PortfolioGithubStatListDto portfolioGithubStatListDto);
    void delete(Long memberId, Long portfolioId);
    Long saveCommitMessages(Long memberId, Long portfolioId, PortfolioCommitMessageListDto portfolioCommitMessageListDto);
}