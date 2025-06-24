package goormton.univ.portfolio.controller;

import goormton.univ.portfolio.dto.PortfolioCreateDto;
import goormton.univ.portfolio.dto.PortfolioResponseDto;
import goormton.univ.portfolio.dto.PortfolioUpdateDto;
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

    // 포트폴리오 수정
    @PutMapping("/{portfolioId}")
    public ResponseEntity<Void> updatePortfolio(@PathVariable Long portfolioId,
                                                @RequestBody PortfolioUpdateDto request) {
        portfolioService.update(portfolioId, request);
        return ResponseEntity.noContent().build();
    }

    // 포트폴리오 삭제
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long portfolioId) {
        portfolioService.delete(portfolioId);
        return ResponseEntity.noContent().build();
    }

    // 포트폴리오 생성
    @PostMapping
    public ResponseEntity<Long> createPortfolio(@RequestBody PortfolioCreateDto portfolioCreateDto) {
        Long createdId = portfolioService.create(portfolioCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdId);
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