package org.zerock.nextenter.apply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyResponse {
    private Long applyId;
    private Long userId;
    private Long jobId;
    private Long resumeId;
    private Long coverLetterId;

    // 지원자 정보
    private String userName;
    private Integer userAge;
    private String userEmail;
    private String userPhone;

    // 공고 정보
    private String jobTitle;
    private String jobCategory;

    // 이력서 인적사항
    private String resumeTitle;
    private String gender;
    private String birthDate;
    private String address;
    private String profileImage;

    // 이력서 스킬 및 경력
    private java.util.List<String> skills;
    private String experience;
    
    // 이력서 상세 정보 (JSON 배열)
    private java.util.List<ExperienceItem> experiences; // 경험/활동/교육
    private java.util.List<CertificateItem> certificates; // 자격증/어학/수상
    private java.util.List<EducationItem> educations; // 학력
    private java.util.List<CareerItem> careers; // 경력

    // 자기소개서 정보
    private String coverLetterTitle;
    private String coverLetterContent;

    // 지원 정보
    private String status;
    private Integer aiScore;
    private String notes;

    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
    
    // 내부 클래스들
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceItem {
        private String title;
        private String period;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateItem {
        private String title;
        private String date;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItem {
        private String school;
        private String period;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareerItem {
        private String company;
        private String period;
    }
}