package com.MajorProject.MainProject.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Topic the user entered to generate the test
    @Column(nullable = false)
    private String topic;

    @Builder.Default
    private Integer totalQuestions = 5;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Integer currentQuestionIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TestStatus status = TestStatus.IN_PROGRESS;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "testSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("questionNumber ASC")
    private List<TestQuestion> questions;

    // ----------------------------------------
    // Enum
    // ----------------------------------------
    public enum TestStatus {
        IN_PROGRESS,
        COMPLETED
    }
}