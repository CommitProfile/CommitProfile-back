package goormton.univ.S3;

import goormton.univ.S3.service.S3AsyncService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class S3UploadUtil {

    private final S3AsyncService s3AsyncService;

    public S3UploadUtil(S3AsyncService s3AsyncService) {
        this.s3AsyncService = s3AsyncService;
    }

    @Async
    public CompletableFuture<String> uploadImageAsync(MultipartFile file, String pathPrefix, String defaultImageUrl) {
        if (file == null || file.isEmpty()) {
            return CompletableFuture.completedFuture(defaultImageUrl);
        }

        try {
            String fileName = pathPrefix + "_" + System.currentTimeMillis();
            return s3AsyncService.uploadFile(fileName, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("파일 업로드 실패: " + e.getMessage()));
            return failedFuture;
        }
    }

    // 동기 업로드 메서드 추가
    public String uploadImageSync(MultipartFile file, String pathPrefix, String defaultImageUrl) {
        if (file == null || file.isEmpty()) {
            return defaultImageUrl;
        }

        try {
            String fileName = pathPrefix + "_" + System.currentTimeMillis();
            // 비동기 메서드를 동기적으로 처리
            return s3AsyncService.uploadFile(fileName, file.getBytes(), file.getContentType()).get();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            // ExecutionException, InterruptedException 등 처리
            throw new RuntimeException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
