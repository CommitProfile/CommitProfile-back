package goormton.univ.portfolio.controller;

import goormton.univ.portfolio.dto.*;
import goormton.univ.portfolio.service.PdfGenerator;
import goormton.univ.portfolio.service.PortfolioService;
import goormton.univ.security.util.JwtUtil;
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

    private final JwtUtil jwtUtil;
    private final PortfolioService portfolioService;
    private final PdfGenerator pdfGenerator;

    // 포트폴리오 조회
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(@RequestHeader("Authorization") String token, @PathVariable Long portfolioId) {
        Long memberId = jwtUtil.extractMemberId(token);
        PortfolioResponseDto portfolio = portfolioService.findById(memberId, portfolioId);
        return ResponseEntity.ok(portfolio);
    }


    // 포트폴리오 삭제
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@RequestHeader("Authorization") String token, @PathVariable Long portfolioId) {
        Long memberId = jwtUtil.extractMemberId(token);
        portfolioService.delete(memberId, portfolioId);
        return ResponseEntity.noContent().build();
    }

    // aboutme 정보 저장
    @PostMapping("/about-me")
    public ResponseEntity<Long> saveUserInfo(@RequestHeader("Authorization") String token, @RequestBody PortfolioAboutMeDto portfolioAboutMeDto) {
        Long memberId = jwtUtil.extractMemberId(token);
        Long portfolioId = portfolioService.saveUserInfo(memberId, portfolioAboutMeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioId);
    }

    // 프로젝트 정보 저장 (서비스 로고 수정 예정)
    @PostMapping("/{portfolioId}/projects")
    public ResponseEntity<Long> saveProjectInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioProjectListDto portfolioProjectListDto) {
        Long memberId = jwtUtil.extractMemberId(token);
        Long updatedId = portfolioService.saveProjectInfo(memberId, portfolioId, portfolioProjectListDto);
        return ResponseEntity.ok(updatedId);
    }

    @PostMapping("/{portfolioId}/github-stats")
    public ResponseEntity<Long> saveGithubStats(
            @RequestHeader("Authorization") String token,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioGithubStatListDto portfolioGithubStatListDto
    ) {
        Long memberId = jwtUtil.extractMemberId(token);
        Long updatedId = portfolioService.saveGithubStats(memberId, portfolioId, portfolioGithubStatListDto);
        return ResponseEntity.ok(updatedId);
    }

    // 커밋 메시지 저장
    @PostMapping("/{portfolioId}/commit-messages")
    public ResponseEntity<Long> saveCommitMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioCommitMessageListDto portfolioCommitMessageListDto
    ) {
        Long memberId = jwtUtil.extractMemberId(token);
        Long updatedId = portfolioService.saveCommitMessages(memberId, portfolioId, portfolioCommitMessageListDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedId);
    }

    // 포트폴리오 PDF 다운로드 (추후 수정 가능)
    @GetMapping("/{portfolioId}/export")
    public ResponseEntity<ByteArrayResource> exportPdf(@RequestHeader("Authorization") String token, @PathVariable Long portfolioId) {
        Long memberId = jwtUtil.extractMemberId(token);
        byte[] pdfBytes = pdfGenerator.generatePortfolioPdf(memberId, portfolioId);
        String fileName = "portfolio.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfBytes));
    }
}