package org.zerock.nextenter.interview.aop;

import org.zerock.nextenter.resume.entity.Resume;

/**
 * 면접 컨텍스트 홀더 (Interview Xpert 참조)
 * 트랜잭션 내에서 이력서 데이터를 스레드 로컬에 저장하여 공유함.
 */
public class InterviewContextHolder {

    private static final ThreadLocal<Resume> resumeContext = new ThreadLocal<>();

    public static void setResume(Resume resume) {
        resumeContext.set(resume);
    }

    public static Resume getResume() {
        return resumeContext.get();
    }

    public static void clear() {
        resumeContext.remove();
    }
}
