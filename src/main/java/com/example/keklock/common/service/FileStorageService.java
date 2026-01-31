package com.example.keklock.common.service;

import com.example.keklock.common.exception.FileUploadException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB
    public static final long MAX_POST_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.avatars-dir}")
    private String avatarsDir;

    @Value("${file.upload.posts-dir}")
    private String postsDir;

    public enum FileType {
        AVATAR,
        POST_IMAGE
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, avatarsDir));
            Files.createDirectories(Paths.get(uploadDir, postsDir));
            log.info("Upload directories initialized successfully");
        } catch (IOException e) {
            throw new FileUploadException("Could not create upload directories", e);
        }
    }

    public String uploadFile(MultipartFile file, FileType type) {
        long maxSize = (type == FileType.AVATAR) ? MAX_AVATAR_SIZE : MAX_POST_IMAGE_SIZE;
        validateImage(file, maxSize);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path uploadPath = getUploadPath(type);

        try {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file: " + fileName, e);
        }
    }

    public void deleteFile(String fileName, FileType type) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        Path filePath = getUploadPath(type).resolve(fileName);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileName);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
        }
    }

    public void validateImage(MultipartFile file, long maxSizeBytes) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new FileUploadException(
                String.format("File size exceeds maximum allowed size of %d MB", maxSizeBytes / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException("Invalid file type. Allowed types: jpg, jpeg, png, gif, webp");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileUploadException("File name is missing");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileUploadException("Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    public String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    public Path getUploadPath(FileType type) {
        String subDir = (type == FileType.AVATAR) ? avatarsDir : postsDir;
        return Paths.get(uploadDir, subDir);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
