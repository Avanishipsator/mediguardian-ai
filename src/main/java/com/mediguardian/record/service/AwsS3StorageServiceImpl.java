package com.mediguardian.record.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AwsS3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${cdn.base.url}")
    private String cdnBaseUrl;

    @Override
    public String uploadFile(MultipartFile file, String fileKey) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return fileKey;
        } catch (IOException | S3Exception e) {
            throw new BusinessException("Failed to upload file to S3: " + e.getMessage(), ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String uploadFile(byte[] fileData, String fileKey, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));
            return fileKey;
        } catch (S3Exception e) {
            throw new BusinessException("Failed to upload file to S3: " + e.getMessage(), ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15)) // URL valid for 15 minutes
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (S3Exception e) {
            throw new BusinessException("Failed to generate pre-signed URL: " + e.getMessage(), ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getCdnUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        return String.format("%s/%s", cdnBaseUrl, fileKey);
    }
}
