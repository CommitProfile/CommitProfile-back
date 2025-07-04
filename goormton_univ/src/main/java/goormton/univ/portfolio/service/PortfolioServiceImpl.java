package goormton.univ.portfolio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.portfolio.dto.*;
import goormton.univ.portfolio.entity.AboutMeField;
import goormton.univ.portfolio.entity.GithubStatField;
import goormton.univ.portfolio.entity.Portfolio;
import goormton.univ.portfolio.entity.ProjectField;
import goormton.univ.portfolio.repository.PortfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Long saveUserInfo(PortfolioAboutMeDto portfolioAboutMeDto) {
        Portfolio portfolio = new Portfolio();
        portfolio.markPortfolioCreated(LocalDateTime.now());

        // 포함된 필드만 골라서 Map으로 구성
        Map<String, String> filtered = new LinkedHashMap<>();
        for (AboutMeField field : portfolioAboutMeDto.getIncludedFields()) {
            String value = extractAboutMeFieldValue(field, portfolioAboutMeDto);
            if (value != null && !value.isBlank()) {
                filtered.put(field.name().toLowerCase(), value);
            }
        }

        try {
            // JSON으로 변환해서 저장
            String userInfoJson = objectMapper.writeValueAsString(filtered);
            portfolio.saveUserInfoJson(userInfoJson);
            Portfolio savedPortfolio = portfolioRepository.save(portfolio);
            return savedPortfolio.getPortfolioId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("UserInfo JSON 변환 실패", e);
        }
    }

    @Override
    public Long saveProjectInfo(Long portfolioId,  PortfolioProjectListDto portfolioProjectListDto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("해당 포트폴리오가 존재하지 않습니다."));

        List<Map<String, String>> projectList = new ArrayList<>();

        for (PortfolioProjectDto projectDto : portfolioProjectListDto.getProjects()) {
            Map<String, String> filtered = new LinkedHashMap<>();
            for (ProjectField field : projectDto.getIncludedFields()) {
                String value = extractProjectFieldValue(field, projectDto);
                if (value != null && !value.isBlank()) {
                    filtered.put(field.name().toLowerCase(), value);
                }
            }
            projectList.add(filtered);
        }

        try {
            String json = objectMapper.writeValueAsString(projectList);
            portfolio.saveProjectsJson(json);
            return portfolioRepository.save(portfolio).getPortfolioId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Projects JSON 변환 실패", e);
        }
    }

    @Override
    public Long saveGithubStats(Long portfolioId, PortfolioGithubStatDto portfolioGithubStatDto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("해당 포트폴리오가 존재하지 않습니다."));

        Map<String, String> filtered = new LinkedHashMap<>();
        for (GithubStatField field : portfolioGithubStatDto.getIncludedFields()) {
            String value = extractGitStatFieldValue(field, portfolioGithubStatDto);
            if (value != null && !value.isBlank()) {
                filtered.put(field.name().toLowerCase(), value);
            }
        }

        try {
            String statsJson = objectMapper.writeValueAsString(filtered);
            portfolio.saveGithubStatsJson(statsJson);
            return portfolioRepository.save(portfolio).getPortfolioId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("GithubStats JSON 변환 실패", e);
        }
    }

    @Override
    public Long saveCommitMessages(Long portfolioId, PortfolioCommitMessageDto dto) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("포트폴리오가 존재하지 않습니다."));

        // included == true 인 메시지만 필터링
        List<String> includedMessages = dto.getMessages().stream()
                .filter(CommitMessageDto::isIncluded)
                .map(CommitMessageDto::getMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .collect(Collectors.toList());

        try {
            String json = objectMapper.writeValueAsString(includedMessages);
            portfolio.saveCommitMessagesJson(json);
            return portfolioRepository.save(portfolio).getPortfolioId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("CommitMessages JSON 변환 실패", e);
        }
    }

    @Override
    public PortfolioResponseDto findById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("포트폴리오가 존재하지 않습니다."));
        return PortfolioResponseDto.fromEntity(portfolio);
    }

    @Override
    public void delete(Long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
    }

    private String extractAboutMeFieldValue(AboutMeField field, PortfolioAboutMeDto dto) {
        return switch (field) {
            case NAME -> dto.getName();
            case SHORTINTRO -> dto.getShortintro();
            case FULLINTRO -> dto.getFullintro();
            case EMAIL -> dto.getEmail();
            case BLOG -> dto.getBlog();
            case YOUTUBE -> dto.getYoutube();
            case GITHUB -> dto.getGithub();
            case INSTAGRAM -> dto.getInstagram();
            case TECHSTACK -> dto.getTechstack();
        };
    }

    private String extractProjectFieldValue(ProjectField field, PortfolioProjectDto dto) {
        return switch (field) {
            case NAME -> dto.getName();
            case TEAM -> dto.getTeam();
            case DESCRIPTION -> dto.getDescription();
            case FEATURE -> dto.getFeature();
            case CONTRIBUTION -> dto.getContribution();
            case STARTDATE -> dto.getStartdate() != null ? dto.getStartdate().toString() : null;
            case ENDDATE -> dto.getEnddate() != null ? dto.getEnddate().toString() : null;
        };
    }

    private String extractGitStatFieldValue(GithubStatField field, PortfolioGithubStatDto dto) {
        return switch (field) {
            case COMMIT -> String.valueOf(dto.getCommitCount());
            case PR -> String.valueOf(dto.getPrCount());
            case MERGE -> String.valueOf(dto.getMergeCount());
            case CONTRIBUTION -> String.valueOf(dto.getContributionRatio());
        };
    }
}