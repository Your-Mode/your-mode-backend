package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.request.s3.*;
import com.yourmode.yourmodebackend.domain.content.dto.response.s3.*;
import com.yourmode.yourmodebackend.domain.content.service.S3Service;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.Map;


@RestController
@RequestMapping("/api/content/s3")
@RequiredArgsConstructor
@Tag(name = "Contents: S3 파일 업로드 및 presigned URL 관리 API", description = "S3 파일 업로드 및 presigned URL 관리 API")
public class S3Controller {

    private final S3Service s3Service;
    
    /**
     * 프론트엔드에서 이미지 업로드에 사용할 S3 presigned URL 발급
     */
    @PostMapping("/presigned-url")
    @Operation(
        summary = "Presigned URL 발급",
        description = "S3에 파일을 업로드하기 위한 presigned URL을 발급합니다. 사용자별 디렉토리(contents/users/{userId}/)에 파일이 저장됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "Presigned URL 발급 성공 예시",
                    summary = "파일 업로드용 Presigned URL 발급 성공",
                    value = """
                {
                    "timestamp": "2025-01-01T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "presignedUrl": "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
                        "fileName": "test-image.jpg",
                        "expirationMinutes": 10
                    }
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "파일명 오류",
                    summary = "파일명 누락 또는 형식 오류",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:00.000",
                    "code": "S3-400-001",
                    "message": "유효하지 않은 파일명입니다."
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "Presigned URL 생성 실패",
                    summary = "S3 서비스 오류로 Presigned URL 생성 실패",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:02.000",
                    "code": "S3-500-002",
                    "message": "Presigned URL 생성에 실패했습니다."
                }
                """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<PresignedUrlResponseDto>> getPresignedUrl(
            @Parameter(
                description = "Presigned URL 발급 요청",
                required = true
            )
            @Valid @RequestBody PresignedUrlRequestDto requestDto
    ) {
        PresignedUrlResponseDto response = s3Service.generatePresignedUrl(requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    @PostMapping("/batch-presigned-urls")
    @Operation(
        summary = "배치 Presigned URL 발급",
        description = "여러 파일을 위한 Presigned URL을 한 번에 발급합니다. 파일명 배열을 Request Body로 전달하세요."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "배치 Presigned URL 발급 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "배치 Presigned URL 발급 성공 예시",
                    summary = "여러 파일의 Presigned URL 발급 성공",
                    value = """
                {
                    "timestamp": "2025-01-01T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "presignedUrls": {
                            "image1.jpg": "https://your-bucket.s3.amazonaws.com/contents/users/123/image1.jpg?X-Amz-Algorithm=...",
                            "image2.jpg": "https://your-bucket.s3.amazonaws.com/contents/users/123/image2.jpg?X-Amz-Algorithm=...",
                            "image3.jpg": "https://your-bucket.s3.amazonaws.com/contents/users/123/image3.jpg?X-Amz-Algorithm=..."
                        },
                        "count": 3,
                        "expirationMinutes": 10
                    }
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "파일 목록 오류",
                    summary = "파일 목록 비어있음 또는 개수 초과",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:00.000",
                    "code": "S3-400-005",
                    "message": "파일명 목록이 비어있습니다."
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "배치 작업 실패",
                    summary = "배치 Presigned URL 생성 중 서버 오류",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:02.000",
                    "code": "S3-500-006",
                    "message": "배치 작업 처리 중 오류가 발생했습니다."
                }
                """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<BatchPresignedUrlResponseDto>> getBatchPresignedUrls(
            @Parameter(
                description = "배치 Presigned URL 발급 요청",
                required = true
            )
            @Valid @RequestBody BatchPresignedUrlRequestDto requestDto
    ) {
        BatchPresignedUrlResponseDto response = s3Service.generateBatchPresignedUrls(requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    /**
     * Presigned URL을 통한 파일 업로드 테스트 (간단한 테스트용)
     */
    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Presigned URL 업로드 테스트",
        description = "Presigned URL을 사용하여 파일을 S3에 업로드하는 테스트 API입니다. 실제 운영에서는 프론트엔드에서 직접 S3에 업로드해야 합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "파일 업로드 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "파일 업로드 성공 예시",
                    summary = "Presigned URL을 통한 파일 업로드 성공",
                    value = """
                {
                    "timestamp": "2025-01-01T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "fileUrl": "https://your-bucket.s3.amazonaws.com/contents/users/123/test.jpg",
                        "fileName": "test.jpg",
                        "fileSize": 1024000
                    }
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "업로드 오류",
                    summary = "Presigned URL 만료 또는 파일 크기 초과",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:00.000",
                    "code": "S3-400-007",
                    "message": "Presigned URL이 만료되었습니다."
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "업로드 실패",
                    summary = "S3 업로드 중 서버 오류",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:02.000",
                    "code": "S3-500-003",
                    "message": "파일 업로드에 실패했습니다."
                }
                """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<Map<String, Object>>> testUpload(
            @Parameter(
                description = "발급받은 S3 presigned URL",
                example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
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
        
        Map<String, Object> result = Map.of(
            "fileUrl", fileUrl,
            "fileName", file.getOriginalFilename(),
            "fileSize", file.getSize()
        );
        
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }

    @DeleteMapping("/delete")
    @Operation(
        summary = "S3 파일 삭제",
        description = "S3에 저장된 파일을 삭제합니다. 컨텐츠 수정 시 기존 이미지를 교체할 때 사용합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "파일 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "파일 삭제 성공 예시",
                    summary = "S3 파일 삭제 성공",
                    value = """
                {
                    "timestamp": "2025-01-01T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "deletedFileUrl": "https://your-bucket.s3.amazonaws.com/contents/users/123/test.jpg",
                        "success": true,
                        "message": "파일이 성공적으로 삭제되었습니다."
                    }
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "삭제 오류",
                    summary = "권한 없음 또는 잘못된 URL 형식",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:00.000",
                    "code": "S3-400-008",
                    "message": "해당 파일에 대한 접근 권한이 없습니다."
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "파일을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "파일 없음",
                    summary = "삭제할 파일이 S3에 존재하지 않는 경우",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:02.000",
                    "code": "S3-404-001",
                    "message": "요청한 파일을 찾을 수 없습니다."
                }
                """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "삭제 실패",
                    summary = "S3 파일 삭제 중 서버 오류",
                    value = """
                {
                    "timestamp": "2025-01-01T12:35:03.000",
                    "code": "S3-500-004",
                    "message": "파일 삭제에 실패했습니다."
                }
                """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<FileDeleteResponseDto>> deleteFile(
            @Parameter(
                description = "S3 파일 삭제 요청",
                required = true
            )
            @Valid @RequestBody FileDeleteRequestDto requestDto
    ) {
        FileDeleteResponseDto response = s3Service.deleteFile(requestDto);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }
} 