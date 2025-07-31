package com.yourmode.yourmodebackend.domain.content.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3ServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public URL generatePresignedUrl(String fileName, Integer userId, int expirationMinutes, String httpMethod) {
        // Create user-specific directory structure
        String userDirectory = "contents/users/" + userId + "/";
        String fullFileName = userDirectory + fileName;
        
        Date expiration = new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fullFileName)
                        .withMethod(com.amazonaws.HttpMethod.valueOf(httpMethod))
                        .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }


    @Override
    public String uploadFileWithPresignedUrl(String presignedUrl, MultipartFile file) {
        try {
            // HTTP 클라이언트를 사용하여 presigned URL로 PUT 요청 (타임아웃 설정 추가)
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(presignedUrl))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .header("Content-Type", file.getContentType())
                    .timeout(java.time.Duration.ofSeconds(60)) // 요청 타임아웃 60초
                    .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Presigned URL에서 실제 S3 URL 추출 (쿼리 파라미터 제거)
                return presignedUrl.split("\\?")[0];
            } else {
                throw new RuntimeException("Failed to upload file using presigned URL. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file using presigned URL", e);
        }
    }
} 