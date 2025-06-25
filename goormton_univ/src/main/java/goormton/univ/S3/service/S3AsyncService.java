package goormton.univ.S3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class S3AsyncService {
    private final S3AsyncClient s3AsyncClient;

    @Value("${aws.s3-bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public S3AsyncService(S3AsyncClient s3AsyncClient) {
        this.s3AsyncClient = s3AsyncClient;
    }

    /**
     * S3에 비동기적으로 파일 업로드
     */
    @Async
    public CompletableFuture<String> uploadFile(String fileName, byte[] fileData, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentDisposition("inline")
                .build();

        return s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromByteBuffer(ByteBuffer.wrap(fileData)))
                .thenApply(response -> {
                    if (response.sdkHttpResponse().isSuccessful()) {
                        return "https://" + bucketName + ".s3." + region + "amazonaws.com/" + fileName;
                    } else {
                        throw new RuntimeException("S3 업로드 실패: " + response.sdkHttpResponse().statusCode());
                    }
                });
    }

    /**
     * S3에서 비동기적으로 파일 삭제
     */
    public CompletableFuture<Void> deleteFile(String key) {
        return s3AsyncClient.listObjectVersions(ListObjectVersionsRequest.builder()
                        .bucket(bucketName)
                        .prefix(key)
                        .build())
                .thenCompose(response -> {
                    List<ObjectIdentifier> objectsToDelete = response.versions().stream()
                            .map(version -> ObjectIdentifier.builder()
                                    .key(version.key())
                                    .versionId(version.versionId())
                                    .build())
                            .collect(Collectors.toList());

                    if (objectsToDelete.isEmpty()) {
                        System.out.println("삭제할 버전 없음: " + key);
                        return CompletableFuture.completedFuture(null);
                    }

                    DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder().objects(objectsToDelete).build())
                            .build();

                    return s3AsyncClient.deleteObjects(deleteObjectsRequest)
                            .thenRun(() -> System.out.println("S3 삭제 성공 (버전 포함): " + key))
                            .exceptionally(ex -> {
                                System.err.println("S3 삭제 실패: " + ex.getMessage());
                                return null;
                            });
                });
    }

    /**
     * 기존 프로필 이미지 삭제 후 새 이미지 업로드
     */
    public CompletableFuture<String> updateProfileImage(String existingImageUrl, String newFileName, byte[] newFileData, String contentType) {
        if (existingImageUrl != null && !existingImageUrl.isBlank()) {
            String oldFileName = extractFileName(existingImageUrl);
            deleteFile(oldFileName).join();
        }
        return uploadFile(newFileName, newFileData, contentType);
    }

    /**
     * S3 URL에서 파일 이름을 추출하는 메서드
     */
    public String extractFileName(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }


    public List<String> listFilesInFolder(String folderPath) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .build();

        ListObjectsV2Response listResponse = s3AsyncClient.listObjectsV2(listRequest).join();

        return listResponse.contents().stream()
                .map(s3Object -> s3Object.key())
                .collect(Collectors.toList());
    }

    public CompletableFuture<Void> deleteFiles(List<String> fileKeys) {
        if (fileKeys.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder()
                        .objects(fileKeys.stream()
                                .map(key -> ObjectIdentifier.builder().key(key).build())
                                .collect(Collectors.toList()))
                        .build())
                .build();

        return s3AsyncClient.deleteObjects(deleteRequest)
                .thenApply(response -> null);
    }


}
