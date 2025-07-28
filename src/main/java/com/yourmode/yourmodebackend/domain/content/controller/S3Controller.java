package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.service.S3Service;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/content/s3")
@RequiredArgsConstructor
@Tag(name = "S3 파일 관리", description = "S3 파일 업로드 및 presigned URL 관리 API")
public class S3Controller {

    private final S3Service s3Service;
    
    /**
     * 프론트엔드에서 S3 presigned URL을 받아 직접 이미지 업로드에 사용
     * @param fileName S3에 저장할 파일명 (예: uuid.jpg)
     * @param expirationMinutes presigned URL 만료 시간(분)
     * @return S3 presigned URL (PUT)
     */
    @GetMapping("/presigned-url")
    @Operation(
        summary = "Presigned URL 발급",
        description = "S3에 파일을 업로드하기 위한 presigned URL을 발급합니다. 사용자별 디렉토리(contents/users/{userId}/)에 파일이 저장됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...")
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(
                description = "S3에 저장할 파일명 (확장자 포함)",
                example = "test-image.jpg",
                required = true
            )
            @RequestParam String fileName,
            @Parameter(
                description = "Presigned URL 만료 시간(분)",
                example = "10"
            )
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
    @Operation(
        summary = "Presigned URL로 파일 업로드",
        description = "발급받은 presigned URL을 사용하여 파일을 S3에 직접 업로드합니다. 업로드된 파일의 실제 S3 URL을 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "파일 업로드 성공",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg")
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (presigned URL 만료 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> uploadFileWithPresignedUrl(
            @Parameter(
                description = "발급받은 S3 presigned URL",
                example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
                required = true
            )
            @RequestParam("presignedUrl") String presignedUrl,
            @Parameter(
                description = "업로드할 파일",
                required = true
            )
            @RequestParam("file") MultipartFile file
    ) {
        String fileUrl = s3Service.uploadFileWithPresignedUrl(presignedUrl, file);
        return ResponseEntity.ok(fileUrl);
    }
} 