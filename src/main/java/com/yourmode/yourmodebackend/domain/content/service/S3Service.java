package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.s3.*;
import com.yourmode.yourmodebackend.domain.content.dto.response.s3.*;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    /**
     * 단일 Presigned URL 생성
     * 
     * @param requestDto Presigned URL 발급 요청 DTO
     * @return Presigned URL 응답 DTO
     */
    PresignedUrlResponseDto generatePresignedUrl(PresignedUrlRequestDto requestDto);
    
    /**
     * 배치 Presigned URL 생성
     * 
     * @param requestDto 배치 Presigned URL 발급 요청 DTO
     * @return 배치 Presigned URL 응답 DTO
     */
    BatchPresignedUrlResponseDto generateBatchPresignedUrls(BatchPresignedUrlRequestDto requestDto);
    
    /**
     * S3 파일 삭제
     * 
     * @param requestDto 파일 삭제 요청 DTO
     * @return 파일 삭제 응답 DTO
     */
    FileDeleteResponseDto deleteFile(FileDeleteRequestDto requestDto);
    
    /**
     * Presigned URL을 사용한 파일 업로드 (테스트용)
     * 
     * @param presignedUrl 발급받은 Presigned URL
     * @param file 업로드할 파일
     * @return 업로드된 파일의 S3 URL
     */
    String uploadFileWithPresignedUrl(String presignedUrl, MultipartFile file);
} 