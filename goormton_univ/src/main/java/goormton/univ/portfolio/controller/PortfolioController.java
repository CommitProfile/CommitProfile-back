package goormton.univ.portfolio.controller;

import goormton.univ.portfolio.dto.*;
import goormton.univ.portfolio.service.PdfGenerator;
import goormton.univ.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PdfGenerator pdfGenerator;

    // 포트폴리오 조회
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(@PathVariable Long portfolioId) {
        PortfolioResponseDto portfolio = portfolioService.findById(portfolioId);
        return ResponseEntity.ok(portfolio);
    }


    // 포트폴리오 삭제
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long portfolioId) {
        portfolioService.delete(portfolioId);
        return ResponseEntity.noContent().build();
    }

    // aboutme 정보 저장
    @PostMapping("/about-me")
    public ResponseEntity<Long> saveUserInfo(@RequestBody PortfolioAboutMeDto dto) {
        Long portfolioId = portfolioService.saveUserInfo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioId);
    }

    // 프로젝트 정보 저장 (서비스 로고 수정 예정)
    @PostMapping("/{portfolioId}/projects")
    public ResponseEntity<Long> saveProjectInfo(
            @PathVariable Long portfolioId,
            @RequestBody PortfolioProjectListDto portfolioProjectListDto) {
        Long updatedId = portfolioService.saveProjectInfo(portfolioId, portfolioProjectListDto);
        return ResponseEntity.ok(updatedId);
    }

    @PostMapping("/{portfolioId}/github-stats")
    public ResponseEntity<Long> saveGithubStats(
            @PathVariable Long portfolioId,
            @RequestBody PortfolioGithubStatDto githubStatDto
    ) {
        Long updatedId = portfolioService.saveGithubStats(portfolioId, githubStatDto);
        return ResponseEntity.ok(updatedId);
    }

    // 커밋 메시지 저장
    @PostMapping("/{portfolioId}/commit-messages")
    public ResponseEntity<Long> saveCommitMessages(
            @PathVariable Long portfolioId,
            @RequestBody PortfolioCommitMessageDto dto
    ) {
        Long updatedId = portfolioService.saveCommitMessages(portfolioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedId);
    }

    // 포트폴리오 PDF 다운로드 (추후 수정 가능)
    @GetMapping("/{portfolioId}/export")
    public ResponseEntity<ByteArrayResource> exportPdf(@PathVariable Long portfolioId) {
        byte[] pdfBytes = pdfGenerator.generatePortfolioPdf(portfolioId);
        String fileName = "portfolio.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfBytes));
    }
}