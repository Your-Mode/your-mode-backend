package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.service.S3Service;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/content/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    
    /**
     * 프론트엔드에서 S3 presigned URL을 받아 직접 이미지 업로드에 사용
     * @param fileName S3에 저장할 파일명 (예: uuid.jpg)
     * @param expirationMinutes presigned URL 만료 시간(분)
     * @return S3 presigned URL (PUT)
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(description = "S3 파일명 (ex: contents/user_id/uuid_filename.jpg)", required = true)
            @RequestParam String fileName,
            @Parameter(description = "만료 시간(분)", example = "10")
            @RequestParam(defaultValue = "10") int expirationMinutes,
            @CurrentUser PrincipalDetails principalDetails
    ) {
        Integer userId = principalDetails.getUserId();
        return ResponseEntity.ok(s3Service.generatePresignedUrl(fileName, userId, expirationMinutes, "PUT").toString());
    }
    
    /**
     * Presigned URL로 파일을 S3에 업로드
     * @param presignedUrl S3 presigned URL
     * @param file 업로드할 파일
     * @return 업로드된 파일의 S3 URL
     */
    @PostMapping(value = "/upload-with-presigned-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Presigned URL로 파일 업로드", description = "Presigned URL을 사용하여 파일을 S3에 업로드합니다.")
    public ResponseEntity<String> uploadFileWithPresignedUrl(
            @Parameter(description = "S3 Presigned URL", required = true)
            @RequestParam("presignedUrl") String presignedUrl,
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        String fileUrl = s3Service.uploadFileWithPresignedUrl(presignedUrl, file);
        return ResponseEntity.ok(fileUrl);
    }
} 