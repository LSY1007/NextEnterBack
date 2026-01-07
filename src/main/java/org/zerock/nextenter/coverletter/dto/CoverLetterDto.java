package org.zerock.nextenter.coverletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.zerock.nextenter.coverletter.entity.CoverLetter;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterDto {
    private Long coverLetterId;
    private String title;
    private String jobCategory;
    private String targetCompany;
    private String content;
    private String filePath;
    private String fileType;
    private Integer wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CoverLetterDto from(CoverLetter entity) {
        return CoverLetterDto.builder()
                .coverLetterId(entity.getCoverLetterId())
                .title(entity.getTitle())
                .jobCategory(entity.getJobCategory())
                .targetCompany(entity.getTargetCompany())
                .content(entity.getContent())
                .filePath(entity.getFilePath())
                .fileType(entity.getFileType())
                .wordCount(entity.getWordCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}