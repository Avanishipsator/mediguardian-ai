package com.mediguardian.record.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String fileName);
    String generatePresignedUrl(String fileKey);
}
