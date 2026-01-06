package org.zerock.nextenter.resume.service;

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

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("hwp", "pdf", "docx", "xlsx", "doc", "xls");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB

    /**
     * 파일 저장
     */
    public String saveFile(MultipartFile file) {
        try {
            validateFile(file);

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", uniqueFilename);
            return uniqueFilename;

        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                return;
            }
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filename);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filename, e);
        }
    }

    /**
     * 파일 검증
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "파일 크기는 10MB를 초과할 수 없습니다 (현재: " +
                            (file.getSize() / 1024 / 1024) + "MB)");
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 형식입니다. 허용된 형식: " + ALLOWED_EXTENSIONS);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("유효하지 않은 파일명입니다");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 파일 URL 생성
     */
    public String getFileUrl(String filename) {
        return uploadDir + "/" + filename;
    }
}