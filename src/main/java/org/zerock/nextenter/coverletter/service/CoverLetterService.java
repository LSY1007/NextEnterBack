package org.zerock.nextenter.coverletter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.coverletter.dto.CoverLetterDto;
import org.zerock.nextenter.coverletter.dto.CoverLetterRequest;
import org.zerock.nextenter.coverletter.entity.CoverLetter;
import org.zerock.nextenter.coverletter.repository.CoverLetterRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final CoverLetterFileService coverLetterFileService;

    public Page<CoverLetterDto> getList(Long userId, Pageable pageable) {
        return coverLetterRepository.findByUserId(userId, pageable)
                .map(CoverLetterDto::from);
    }

    public CoverLetterDto getDetail(Long userId, Long id) {
        CoverLetter coverLetter = findByIdAndUserId(id, userId);
        return CoverLetterDto.from(coverLetter);
    }

    @Transactional
    public Long create(Long userId, CoverLetterRequest request) {
        CoverLetter coverLetter = CoverLetter.builder()
                .userId(userId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                .targetCompany(request.getTargetCompany())
                .content(request.getContent())
                .wordCount(request.getContent() != null ? request.getContent().length() : 0)
                .build();

        return coverLetterRepository.save(coverLetter).getCoverLetterId();
    }

    @Transactional
    public void update(Long userId, Long id, CoverLetterRequest request) {
        CoverLetter coverLetter = findByIdAndUserId(id, userId);
        coverLetter.update(
                request.getTitle(),
                request.getJobCategory(),
                request.getTargetCompany(),
                request.getContent()
        );
    }

    @Transactional
    public void delete(Long userId, Long id) {
        CoverLetter coverLetter = findByIdAndUserId(id, userId);

        // 파일이 있으면 물리적 삭제
        if (coverLetter.getFilePath() != null) {
            coverLetterFileService.deletePhysicalFile(coverLetter.getFilePath());
        }

        coverLetterRepository.delete(coverLetter);
    }

    private CoverLetter findByIdAndUserId(Long id, Long userId) {
        return coverLetterRepository.findByCoverLetterIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("자소서를 찾을 수 없습니다"));
    }
}