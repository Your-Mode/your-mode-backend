package com.yourmode.yourmodebackend.domain.content.service;

import org.springframework.web.multipart.MultipartFile;
import java.net.URL;

public interface S3Service {
    URL generatePresignedUrl(String fileName, Integer userId, int expirationMinutes, String httpMethod);
    String uploadFileWithPresignedUrl(String presignedUrl, MultipartFile file);
} 