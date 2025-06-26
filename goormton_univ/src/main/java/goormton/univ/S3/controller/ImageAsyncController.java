package goormton.univ.S3.controller;

import goormton.univ.S3.dto.S3ResponseDto;
import goormton.univ.S3.service.S3AsyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async/images")
public class ImageAsyncController {

    private final S3AsyncService s3AsyncService;

    public ImageAsyncController(S3AsyncService s3AsyncService) {
        this.s3AsyncService = s3AsyncService;
    }

    /**
     * 비동기적으로 이미지 업로드
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<S3ResponseDto>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            byte[] fileBytes = file.getBytes();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            return s3AsyncService.uploadFile(fileName, fileBytes, contentType)
                    .thenApply(url -> ResponseEntity.ok(
                            S3ResponseDto.builder()
                                    .message("File uploaded successfully")
                                    .url(url)
                                    .fileName(fileName)
                                    .build()
                    ));
        } catch (IOException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(
                    S3ResponseDto.builder()
                            .message("File upload failed")
                            .error(e.getMessage())
                            .build()
            ));
        }
    }


    /**
     * 비동기적으로 이미지 삭제
     */
    @DeleteMapping("/delete")
    public CompletableFuture<ResponseEntity<S3ResponseDto>> deleteImage(@RequestParam("fileName") String fileName) {
        return s3AsyncService.deleteFile(fileName)
                .thenApply(voidRes -> ResponseEntity.ok(
                        S3ResponseDto.builder()
                                .message("File deleted successfully")
                                .fileName(fileName)
                                .build()
                ))
                .exceptionally(ex -> ResponseEntity.badRequest().body(
                        S3ResponseDto.builder()
                                .message("File delete failed")
                                .fileName(fileName)
                                .error(ex.getMessage())
                                .build()
                ));
    }
}
