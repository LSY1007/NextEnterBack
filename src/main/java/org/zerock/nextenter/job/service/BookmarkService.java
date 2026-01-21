package org.zerock.nextenter.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.job.dto.BookmarkDto;
import org.zerock.nextenter.job.dto.BookmarkedJobDto;
import org.zerock.nextenter.job.entity.Bookmark;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.BookmarkRepository;
import org.zerock.nextenter.job.repository.JobPostingRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;  // 추가

    /**
     * 북마크 추가
     */
    @Transactional
    public BookmarkDto addBookmark(Long userId, Long jobPostingId) {
        // 1. 공고 존재 확인
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        // 2. 이미 북마크했는지 확인
        if (bookmarkRepository.existsByUserIdAndJobPostingId(userId, jobPostingId)) {
            throw new IllegalArgumentException("이미 북마크한 공고입니다");
        }

        // 3. 북마크 생성
        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .jobPostingId(jobPostingId)
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);

        // 4. JobPosting의 bookmarkCount 증가
        jobPosting.setBookmarkCount(jobPosting.getBookmarkCount() + 1);
        jobPostingRepository.save(jobPosting); // 명시적으로 저장

        return BookmarkDto.from(saved);
    }

    /**
     * 북마크 삭제
     */
    @Transactional
    public void removeBookmark(Long userId, Long jobPostingId) {
        Bookmark bookmark = bookmarkRepository.findByUserIdAndJobPostingId(userId, jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("북마크를 찾을 수 없습니다"));

        bookmarkRepository.delete(bookmark);

        // JobPosting의 bookmarkCount 감소
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId).orElse(null);
        if (jobPosting != null && jobPosting.getBookmarkCount() > 0) {
            jobPosting.setBookmarkCount(jobPosting.getBookmarkCount() - 1);
            jobPostingRepository.save(jobPosting); // 명시적으로 저장
        }
    }

    /**
     * 북마크 토글 (있으면 삭제, 없으면 추가)
     */
    @Transactional
    public BookmarkDto toggleBookmark(Long userId, Long jobPostingId) {
        log.info("북마크 토글 - userId: {}, jobPostingId: {}", userId, jobPostingId);
        
        try {
            // 공고 존재 확인
            JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                    .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

            Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndJobPostingId(userId, jobPostingId);

            if (existing.isPresent()) {
                // 이미 있으면 삭제
                log.info("북마크 삭제 - bookmarkId: {}", existing.get().getBookmarkId());
                bookmarkRepository.delete(existing.get());
                if (jobPosting.getBookmarkCount() > 0) {
                    jobPosting.setBookmarkCount(jobPosting.getBookmarkCount() - 1);
                    jobPostingRepository.save(jobPosting);
                }
                log.info("북마크 삭제 완료 - 현재 북마크 수: {}", jobPosting.getBookmarkCount());
                return null;
            } else {
                // 없으면 추가
                log.info("북마크 추가 시작");
                Bookmark bookmark = Bookmark.builder()
                        .userId(userId)
                        .jobPostingId(jobPostingId)
                        .build();

                Bookmark saved = bookmarkRepository.save(bookmark);
                jobPosting.setBookmarkCount(jobPosting.getBookmarkCount() + 1);
                jobPostingRepository.save(jobPosting);
                log.info("북마크 추가 완료 - bookmarkId: {}, 현재 북마크 수: {}", 
                        saved.getBookmarkId(), jobPosting.getBookmarkCount());
                return BookmarkDto.from(saved);
            }
        } catch (Exception e) {
            log.error("북마크 토글 실패 - userId: {}, jobPostingId: {}", userId, jobPostingId, e);
            throw e;
        }
    }

    /**
     * 북마크 목록 조회 (공고 정보 포함)
     */
    public Page<BookmarkedJobDto> getBookmarkedJobs(Long userId, Pageable pageable) {
        // 1. 사용자의 북마크 목록 조회
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        if (bookmarks.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 북마크된 공고 ID 목록
        List<Long> jobPostingIds = bookmarks.getContent().stream()
                .map(Bookmark::getJobPostingId)
                .collect(Collectors.toList());

        // 3. 공고 정보 조회
        List<JobPosting> jobPostings = jobPostingRepository.findAllById(jobPostingIds);

        // 4. 공고 Map으로 변환 (빠른 조회)
        Map<Long, JobPosting> jobPostingMap = jobPostings.stream()
                .collect(Collectors.toMap(JobPosting::getJobId, jp -> jp));

        // 5. 회사 ID 목록 추출
        List<Long> companyIds = jobPostings.stream()
                .map(JobPosting::getCompanyId)
                .distinct()
                .collect(Collectors.toList());

        // 6. 회사 정보 조회
        List<Company> companies = companyRepository.findAllById(companyIds);
        Map<Long, String> companyNameMap = companies.stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getCompanyName));

        // 7. DTO 변환
        List<BookmarkedJobDto> dtos = bookmarks.getContent().stream()
                .map(bookmark -> {
                    JobPosting job = jobPostingMap.get(bookmark.getJobPostingId());
                    if (job == null) {
                        return null;  // 공고가 삭제된 경우
                    }

                    // 회사명 조회
                    String companyName = companyNameMap.getOrDefault(job.getCompanyId(), "알 수 없음");

                    // 연봉 문자열 생성
                    String salaryStr = formatSalary(job.getSalaryMin(), job.getSalaryMax());

                    // 경력 문자열 생성
                    String experienceStr = formatExperience(job.getExperienceMin(), job.getExperienceMax());

                    return BookmarkedJobDto.builder()
                            .bookmarkId(bookmark.getBookmarkId())
                            .bookmarkedAt(bookmark.getCreatedAt())
                            .jobPostingId(job.getJobId())
                            .title(job.getTitle())
                            .companyName(companyName)
                            .location(job.getLocation())
                            .experienceLevel(experienceStr)
                            .salary(salaryStr)
                            .jobType(job.getJobCategory())
                            .deadline(job.getDeadline() != null ? job.getDeadline().atStartOfDay() : null)
                            .status(job.getStatus().name())
                            .build();
                })
                .filter(dto -> dto != null)  // null 제거
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, bookmarks.getTotalElements());
    }

    /**
     * 북마크 여부 확인
     */
    public boolean isBookmarked(Long userId, Long jobPostingId) {
        return bookmarkRepository.existsByUserIdAndJobPostingId(userId, jobPostingId);
    }

    /**
     * 여러 공고의 북마크 여부 확인 (Map 반환)
     */
    public Map<Long, Boolean> getBookmarkStatusMap(Long userId, List<Long> jobPostingIds) {
        // 사용자가 북마크한 공고 ID 목록
        List<Long> bookmarkedIds = bookmarkRepository.findJobPostingIdsByUserId(userId);
        Set<Long> bookmarkedSet = Set.copyOf(bookmarkedIds);

        // 각 공고 ID에 대해 북마크 여부 Map 생성
        return jobPostingIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        bookmarkedSet::contains
                ));
    }

    /**
     * 사용자의 총 북마크 수
     */
    public Long getBookmarkCount(Long userId) {
        return bookmarkRepository.countByUserId(userId);
    }

    /**
     * 특정 공고의 북마크 수
     */
    public Long getJobBookmarkCount(Long jobPostingId) {
        return bookmarkRepository.countByJobPostingId(jobPostingId);
    }

    // Helper 메서드들

    private String formatSalary(Integer min, Integer max) {
        if (min == null && max == null) {
            return "협의";
        }
        if (min == null) {
            return max + "만원 이하";
        }
        if (max == null) {
            return min + "만원 이상";
        }
        return min + "~" + max + "만원";
    }

    private String formatExperience(Integer min, Integer max) {
        if (min == null || min == 0) {
            if (max == null) {
                return "경력 무관";
            }
            return max + "년 이하";
        }
        if (max == null) {
            return "경력 " + min + "년 이상";
        }
        if (min.equals(max)) {
            return "경력 " + min + "년";
        }
        return "경력 " + min + "~" + max + "년";
    }
}