package org.zerock.nextenter.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "job_category", length = 50, nullable = false)
    private String jobCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Difficulty difficulty = Difficulty.JUNIOR;

    @Column(name = "total_turns", nullable = false)
    @Builder.Default
    private Integer totalTurns = 5;

    @Column(name = "current_turn", nullable = false)
    @Builder.Default
    private Integer currentTurn = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.IN_PROGRESS;

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "final_feedback", columnDefinition = "TEXT")
    private String finalFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum Difficulty {
        JUNIOR, SENIOR
    }

    public enum Status {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.difficulty == null) {
            this.difficulty = Difficulty.JUNIOR;
        }
        if (this.status == null) {
            this.status = Status.IN_PROGRESS;
        }
        if (this.totalTurns == null) {
            this.totalTurns = 5;
        }
        if (this.currentTurn == null) {
            this.currentTurn = 0;
        }
    }

    // 비즈니스 메서드
    public void completeInterview(Integer score, String feedback) {
        this.status = Status.COMPLETED;
        this.finalScore = score;
        this.finalFeedback = feedback;
        this.completedAt = LocalDateTime.now();
    }

    public void cancelInterview() {
        this.status = Status.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public void incrementTurn() {
        this.currentTurn++;
    }

    public boolean isCompleted() {
        return this.status == Status.COMPLETED || this.currentTurn >= this.totalTurns;
    }
}