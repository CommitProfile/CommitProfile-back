package goormton.univ.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.portfolio.entity.Portfolio;
import goormton.univ.portfolio.repository.PortfolioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PdfGenerator {

    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;

    public void validateOwnership(Long memberId, Portfolio portfolio) {
        if (!portfolio.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("해당 포트폴리오에 접근할 권한이 없습니다.");
        }
    }

    public byte[] generatePortfolioPdf(Long memberId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 포트폴리오가 존재하지 않습니다."));
        validateOwnership(memberId, portfolio);

        String htmlContent = generateHtmlFromPortfolio(portfolio);
        try {
            return generatePdfFromHtml(htmlContent);
        } catch (Exception e) {
            throw new RuntimeException("PDF 변환 중 오류 발생", e);
        }
    }

    private byte[] generatePdfFromHtml(String htmlContent) {
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

    private String generateHtmlFromPortfolio(Portfolio portfolio) {
        StringBuilder html = new StringBuilder();

        html.append("""
            <html>
                <head>
                    <style>
                        body { font-family: 'Arial', sans-serif; padding: 20px; }
                        h1 { color: #333; }
                        h2 { color: #555; }
                        p, li { font-size: 14px; line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <h1>포트폴리오</h1>
                    <h2>생성일: %s</h2>
                """.formatted(portfolio.getCreatedAt()));

        // 유저 정보
        html.append("<h2>About Me</h2>");
        try {
            Map<String, String> userInfo = objectMapper.readValue(
                    portfolio.getUserInfoJson(), new TypeReference<>() {});
            for (Map.Entry<String, String> entry : userInfo.entrySet()) {
                html.append("<p><strong>")
                        .append(entry.getKey())
                        .append(":</strong> ")
                        .append(entry.getValue())
                        .append("</p>");
            }
        } catch (Exception e) {
            html.append("<p>사용자 정보 파싱 실패</p>");
        }

        // 프로젝트 정보
        html.append("<h2>Projects</h2>");
        try {
            List<Map<String, String>> projects = objectMapper.readValue(
                    portfolio.getProjectsJson(), new TypeReference<List<Map<String, String>>>() {});
            for (Map<String, String> project : projects) {
                html.append("<ul>");
                for (Map.Entry<String, String> entry : project.entrySet()) {
                    html.append("<li><strong>")
                            .append(entry.getKey())
                            .append(":</strong> ")
                            .append(entry.getValue())
                            .append("</li>");
                }
                html.append("</ul><br/>");
            }
        } catch (Exception e) {
            html.append("<p>프로젝트 정보 파싱 실패</p>");
        }

        html.append("</body></html>");
        return html.toString();
    }
}
