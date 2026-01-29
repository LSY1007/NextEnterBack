package org.zerock.nextenter.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequest {

    @NotBlank(message = "이력서 제목은 필수입니다")
    private String title;

    private String jobCategory;

    // ===== 개인정보 필드들 =====
    
    private String resumeName; // 이름
    private String resumeGender; // 성별
    private String resumeBirthDate; // 생년월일
    private String resumeEmail; // 이메일
    private String resumePhone; // 연락처
    private String resumeAddress; // 주소
    private String resumeDetailAddress; // 상세주소
    private String profileImage; // 프로필 이미지 (Base64)

    // ===== 분리된 섹션들 (JSON 문자열로 전달) =====

    // 경험/활동/교육 JSON 문자열
    // 예: "[{\"title\":\"봉사활동\",\"period\":\"2020.01 - 2021.01\"}]"
    private String experiences;

    // 자격증/어학/수상 JSON 문자열
    // 예: "[{\"title\":\"TOEIC 900\",\"date\":\"2020.01\"}]"
    private String certificates;

    // 학력 JSON 문자열
    // 예: "[{\"school\":\"서울대학교\",\"major\":\"컴퓨터공학\",\"period\":\"2015 ~ 2019\"}]"
    private String educations;

    // 경력 JSON 문자열
    // 예: "[{\"company\":\"네이버\",\"position\":\"선임연구원\",\"role\":\"백엔드개발\",\"period\":\"2019.01~2023.01\"}]"
    private String careers;

    // ===== 기존 필드들 =====

    // 기술 스택 (쉼표로 구분된 문자열)
    private String skills;  // "React, Node.js, AWS"

    // 공개/비공개 설정
    private String visibility;  // PUBLIC, PRIVATE

    private String status;  // DRAFT, COMPLETED

    // 기존 structuredData (하위 호환성을 위해 유지)
    @Deprecated
    private String sections;  // structuredData로 저장됨
}