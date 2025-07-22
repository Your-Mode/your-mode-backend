package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.service.S3Service;
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
public class S3TestController {

    private final S3Service s3Service;

    @Operation(
        summary = "S3 파일 업로드 테스트",
        description = "multipart/form-data로 파일을 업로드하면 S3 URL을 반환합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "업로드 성공",
                content = @Content(schema = @Schema(implementation = String.class))
            )
        }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
        @Parameter(description = "업로드할 파일", required = true)
        @RequestPart("file") MultipartFile file,
        @Parameter(description = "S3 내 폴더명", example = "test")
        @RequestParam(value = "dir", defaultValue = "test") String dir
    ) {
        String url = s3Service.uploadFile(file, dir);
        return ResponseEntity.ok(url);
    }
    
    @Operation(
        summary = "S3 presigned URL 발급",
        description = "파일명과 만료시간(분)을 받아 presigned URL을 반환합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "URL 발급 성공",
                content = @Content(schema = @Schema(implementation = String.class))
            )
        }
    )
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(description = "S3 파일명 (ex: test/uuid_filename.jpg)", required = true)
            @RequestParam String fileName,
            @Parameter(description = "만료 시간(분)", example = "10")
            @RequestParam(defaultValue = "10") int expirationMinutes
    ) {
        return ResponseEntity.ok(s3Service.generatePresignedUrl(fileName, expirationMinutes).toString());
    }
} 