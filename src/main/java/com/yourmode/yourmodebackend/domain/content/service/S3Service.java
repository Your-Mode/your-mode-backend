package com.yourmode.yourmodebackend.domain.content.service;

import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

public interface S3Service {
    String uploadFile(MultipartFile file, String dirName);
    void deleteFile(String fileUrl);
    URL generatePresignedUrl(String fileName, int expirationMinutes);
} 