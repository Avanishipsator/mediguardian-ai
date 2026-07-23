package com.mediguardian.record.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String fileName);
    String uploadFile(byte[] fileData, String fileName, String contentType);
    String generatePresignedUrl(String fileKey);
    String getCdnUrl(String fileKey);
}
