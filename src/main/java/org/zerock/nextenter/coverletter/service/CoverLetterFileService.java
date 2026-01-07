package org.zerock.nextenter.coverletter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.coverletter.dto.CoverLetterUploadResponse;
import org.zerock.nextenter.coverletter.entity.CoverLetter;
import org.zerock.nextenter.coverletter.repository.CoverLetterRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoverLetterFileService {

    private final CoverLetterRepository coverLetterRepository;

    @Value("${file.upload-dir:uploads/coverletters}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "hwp");

    @Transactional
    public CoverLetterUploadResponse uploadFile(Long userId, MultipartFile file) {
        // 1. 파일 검증
        validateFile(file);

        // 2. 파일 저장
        String fileName = generateFileName(file);
        String filePath = saveFile(file, fileName);
        String fileType = getFileExtension(file);

        // 3. CoverLetter 엔티티 생성
        CoverLetter coverLetter = CoverLetter.builder()
                .userId(userId)
                .title(file.getOriginalFilename())
                .filePath(filePath)
                .fileType(fileType)
                .wordCount(0)
                .build();

        coverLetterRepository.save(coverLetter);

        // 4. 응답 생성
        return CoverLetterUploadResponse.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(file.getOriginalFilename())
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(file.getSize())
                .message("파일이 업로드되었습니다")
                .build();
    }

    @Transactional
    public void deleteFile(Long userId, Long coverLetterId) {
        CoverLetter coverLetter = coverLetterRepository.findByCoverLetterIdAndUserId(coverLetterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("자소서를 찾을 수 없습니다"));

        // 물리적 파일 삭제
        if (coverLetter.getFilePath() != null) {
            deletePhysicalFile(coverLetter.getFilePath());
        }

        // DB 업데이트
        coverLetter.removeFile();
    }

    public Resource downloadFile(Long userId, Long coverLetterId) {
        CoverLetter coverLetter = coverLetterRepository.findByCoverLetterIdAndUserId(coverLetterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("자소서를 찾을 수 없습니다"));

        if (coverLetter.getFilePath() == null) {
            throw new IllegalArgumentException("파일이 존재하지 않습니다");
        }

        try {
            Path filePath = Paths.get(coverLetter.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("파일을 읽을 수 없습니다");
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }

    // Private 메서드들
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        String extension = getFileExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (PDF, DOCX, HWP만 가능)");
        }
    }

    private String saveFile(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    public void deletePhysicalFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 로그만 남기고 예외는 발생시키지 않음
            System.err.println("파일 삭제 실패: " + filePath);
        }
    }

    private String generateFileName(MultipartFile file) {
        String extension = getFileExtension(file);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}