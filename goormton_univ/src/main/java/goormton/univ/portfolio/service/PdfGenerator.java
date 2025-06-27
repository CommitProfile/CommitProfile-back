package goormton.univ.portfolio.service;

import goormton.univ.portfolio.entity.Portfolio;
import goormton.univ.portfolio.repository.ProtfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class PdfGenerator {

    private final ProtfolioRepository portfolioRepository;

    public byte[] generatePortfolioPdf(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 포트폴리오가 존재하지 않습니다."));

        String htmlContent = generateHtmlFromPortfolio(portfolio);
        try {
            return generatePdfFromHtml(htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("PDF 변환 중 오류 발생", e);
        }
    }

    // HTML을 PDF로 변환
    public byte[] generatePdfFromHtml(String htmlContent) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(os);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    // HTML 생성 함수
    private String generateHtmlFromPortfolio(Portfolio portfolio) {
        return """
            <html>
                <head>
                    <style>
                        body { font-family: 'Arial', sans-serif; padding: 20px; }
                        h1 { color: #333; }
                        p { font-size: 14px; }
                    </style>
                </head>
                <body>
                    <h1>포트폴리오</h1>
                    <p>%s</p>
                </body>
            </html>
            """.formatted(portfolio.getPortfolioText());
    }
}
