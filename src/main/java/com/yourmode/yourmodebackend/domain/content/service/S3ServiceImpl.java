package com.yourmode.yourmodebackend.domain.content.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.yourmode.yourmodebackend.domain.content.dto.request.s3.*;
import com.yourmode.yourmodebackend.domain.content.dto.response.s3.*;
import com.yourmode.yourmodebackend.domain.content.status.S3ErrorStatus;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    
    private final AmazonS3 amazonS3;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public PresignedUrlResponseDto generatePresignedUrl(PresignedUrlRequestDto requestDto) {
        Integer userId = requestDto.getUserId();
        
        try {
            // 사용자 ID 존재 여부 확인
            validateUserExists(userId);
            
            // 파일명 유효성 검사
            validateFileName(requestDto.getFileName());
            
            // 만료 시간 유효성 검사
            validateExpirationTime(requestDto.getExpirationMinutes());
            
            // Presigned URL 생성
            URL presignedUrl = createPresignedUrl(
                requestDto.getFileName(), 
                userId, 
                requestDto.getExpirationMinutes()
            );
            
            return PresignedUrlResponseDto.builder()
                    .presignedUrl(presignedUrl.toString())
                    .fileName(requestDto.getFileName())
                    .expirationMinutes(requestDto.getExpirationMinutes())
                    .build();
                    
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - userId: {}, fileName: {}, error: {}", 
                     userId, requestDto.getFileName(), e.getMessage());
            throw new RestApiException(S3ErrorStatus.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    @Override
    public BatchPresignedUrlResponseDto generateBatchPresignedUrls(BatchPresignedUrlRequestDto requestDto) {
        Integer userId = requestDto.getUserId();
        
        try {
            // 사용자 ID 존재 여부 확인
            validateUserExists(userId);
            
            // 파일 목록 유효성 검사
            validateFileList(requestDto.getFileNames());
            
            // 만료 시간 유효성 검사
            validateExpirationTime(requestDto.getExpirationMinutes());
            
            // 배치 Presigned URL 생성
            Map<String, String> presignedUrls = createBatchPresignedUrls(
                requestDto.getFileNames(), 
                userId, 
                requestDto.getExpirationMinutes()
            );
            
            return BatchPresignedUrlResponseDto.builder()
                    .presignedUrls(presignedUrls)
                    .count(requestDto.getFileNames().size())
                    .expirationMinutes(requestDto.getExpirationMinutes())
                    .build();
                    
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("배치 Presigned URL 생성 실패 - userId: {}, fileCount: {}, error: {}", 
                     userId, requestDto.getFileNames().size(), e.getMessage());
            throw new RestApiException(S3ErrorStatus.BATCH_OPERATION_FAILED);
        }
    }

    @Override
    public FileDeleteResponseDto deleteFile(FileDeleteRequestDto requestDto) {
        Integer userId = requestDto.getUserId();
        
        try {
            // 사용자 ID 존재 여부 확인
            validateUserExists(userId);
            
            // 파일 URL 유효성 검사
            validateFileUrl(requestDto.getFileUrl());
            
            // 사용자 권한 확인
            validateUserAccess(requestDto.getFileUrl(), userId);
            
            // 파일 삭제 실행
            deleteFileFromS3(requestDto.getFileUrl());
            
            return FileDeleteResponseDto.builder()
                    .deletedFileUrl(requestDto.getFileUrl())
                    .success(true)
                    .message("파일이 성공적으로 삭제되었습니다.")
                    .build();
                    
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("파일 삭제 실패 - userId: {}, fileUrl: {}, error: {}", 
                     userId, requestDto.getFileUrl(), e.getMessage());
            throw new RestApiException(S3ErrorStatus.FILE_DELETION_FAILED);
        }
    }

    @Override
    public String uploadFileWithPresignedUrl(String presignedUrl, MultipartFile file) {
        try {
            // HTTP 클라이언트 설정
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(presignedUrl))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .header("Content-Type", file.getContentType())
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            // 요청 실행
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Presigned URL에서 실제 S3 URL 추출 (쿼리 파라미터 제거)
                return presignedUrl.split("\\?")[0];
            } else {
                log.error("Presigned URL 업로드 실패 - statusCode: {}, fileName: {}", 
                         response.statusCode(), file.getOriginalFilename());
                throw new RestApiException(S3ErrorStatus.FILE_UPLOAD_FAILED);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Presigned URL 업로드 중 오류 발생 - fileName: {}, error: {}", 
                     file.getOriginalFilename(), e.getMessage());
            throw new RestApiException(S3ErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    // Private helper methods
    private URL createPresignedUrl(String fileName, Integer userId, int expirationMinutes) {
        String userDirectory = "contents/users/" + userId + "/";
        String fullFileName = userDirectory + fileName;
        
        Date expiration = new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fullFileName)
                        .withMethod(com.amazonaws.HttpMethod.PUT)
                        .withExpiration(expiration);
        
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    private Map<String, String> createBatchPresignedUrls(List<String> fileNames, Integer userId, int expirationMinutes) {
        Map<String, String> presignedUrls = new HashMap<>();
        String userDirectory = "contents/users/" + userId + "/";
        
        for (String fileName : fileNames) {
            String fullFileName = userDirectory + fileName;
            Date expiration = new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L);
            
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, fullFileName)
                            .withMethod(com.amazonaws.HttpMethod.PUT)
                            .withExpiration(expiration);
            
            URL presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            presignedUrls.put(fileName, presignedUrl.toString());
        }
        
        return presignedUrls;
    }

    private void deleteFileFromS3(String fileUrl) {
        try {
            // S3 URL에서 키 추출 (https://bucket.s3.region.amazonaws.com/path/to/file.jpg)
            String key = fileUrl.replace("https://" + bucket + ".s3.amazonaws.com/", "");
            amazonS3.deleteObject(bucket, key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패 - fileUrl: {}, error: {}", fileUrl, e.getMessage());
            throw new RestApiException(S3ErrorStatus.FILE_DELETION_FAILED);
        }
    }

    // Validation methods
    private void validateUserExists(Integer userId) {
        if (userId == null) {
            throw new RestApiException(S3ErrorStatus.INVALID_USER_ID);
        }
        if (!userRepository.existsById(userId)) {
            throw new RestApiException(S3ErrorStatus.USER_NOT_FOUND);
        }
    }
    
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RestApiException(S3ErrorStatus.INVALID_FILE_NAME);
        }
        if (fileName.length() > 255) {
            throw new RestApiException(S3ErrorStatus.INVALID_FILE_NAME);
        }
    }
    
    private void validateExpirationTime(Integer expirationMinutes) {
        if (expirationMinutes == null || expirationMinutes < 1 || expirationMinutes > 60) {
            throw new RestApiException(S3ErrorStatus.INVALID_EXPIRATION_TIME);
        }
    }
    
    private void validateFileList(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            throw new RestApiException(S3ErrorStatus.EMPTY_FILE_LIST);
        }
        if (fileNames.size() > 20) {
            throw new RestApiException(S3ErrorStatus.TOO_MANY_FILES);
        }
        for (String fileName : fileNames) {
            validateFileName(fileName);
        }
    }
    
    private void validateFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new RestApiException(S3ErrorStatus.INVALID_FILE_URL);
        }
        if (!fileUrl.contains(".s3.amazonaws.com/")) {
            throw new RestApiException(S3ErrorStatus.INVALID_FILE_URL);
        }
    }
    
    private void validateUserAccess(String fileUrl, Integer userId) {
        if (!fileUrl.contains("/contents/users/" + userId + "/")) {
            throw new RestApiException(S3ErrorStatus.UNAUTHORIZED_FILE_ACCESS);
        }
    }
} 