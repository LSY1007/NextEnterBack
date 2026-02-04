package org.zerock.nextenter.resume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {

    private Long resumeId;
    private String title;
    private String jobCategory;

    // ===== 사용자 기본 정보 (User 테이블에서 조회) - 하위호환용 =====
    @Deprecated
    private String userName;      // User.name
    @Deprecated
    private String userEmail;     // User.email
    @Deprecated
    private String userGender;    // User.gender
    @Deprecated
    private String userPhone;     // User.phone
    @Deprecated
    private Integer userAge;      // User.age
    @Deprecated
    private String userBio;       // User.bio

    // ===== 이력서별 개인정보 (Resume 테이블에 저장된 정보) =====
    private String resumeName;           // 이름
    private String resumeGender;         // 성별
    private String resumeBirthDate;      // 생년월일
    private String resumeEmail;          // 이메일
    private String resumePhone;          // 연락처
    private String resumeAddress;        // 주소
    private String resumeDetailAddress;  // 상세주소
    private String profileImage;         // 프로필 이미지
    private String desiredSalary;        // 희망 연봉

    // ===== 분리된 섹션들 =====

    // 경험/활동/교육
    private String experiences;
    // 예: [{"title":"봉사활동","period":"2020.01 - 2021.01"}]

    // 자격증/어학/수상
    private String certificates;
    // 예: [{"title":"TOEIC 900","date":"2020.01"}]

    // 학력
    private String educations;
    // 예: [{"school":"서울대학교","major":"컴퓨터공학","period":"2015 ~ 2019"}]

    // 경력
    private String careers;
    // 예: [{"company":"네이버","position":"선임연구원","role":"백엔드 개발","period":"2019.01 ~ 2023.01"}]

    // ===== 기존 필드들 =====

    // AI 처리 결과
    private String extractedText;
    private String skills;
    private String resumeRecommend;

    // 파일 정보
    private String filePath;
    private String fileType;

    // 메타 정보
    private Boolean isMain;
    private String visibility;
    private Integer viewCount;
    private String status;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ 포트폴리오 목록
    private List<PortfolioInfo> portfolios;

    // ✅ 자기소개서 목록
    private List<CoverLetterInfo> coverLetters;

    // 기존 structuredData (하위 호환성을 위해 유지)
    @Deprecated
    private String structuredData;

    // ✅ 포트폴리오 정보 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioInfo {
        private Long portfolioId;
        private String filename;
        private String filePath;
        private String fileType;
        private Long fileSize;
        private String description;
        private Integer displayOrder;
    }

    // ✅ 자기소개서 정보 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoverLetterInfo {
        private Long coverLetterId;
        private String title;
        private String content;
        private String filePath;
        private String fileType;
    }
}