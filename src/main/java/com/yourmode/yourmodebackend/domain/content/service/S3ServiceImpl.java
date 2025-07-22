package com.yourmode.yourmodebackend.domain.content.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
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
    public String uploadFile(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    @Override
    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    @Override
    public URL generatePresignedUrl(String fileName, int expirationMinutes) {
        Date expiration = new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000L);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }
} 