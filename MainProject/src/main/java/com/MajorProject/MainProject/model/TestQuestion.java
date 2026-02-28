package com.MajorProject.MainProject.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "test_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer questionNumber;  // 1 to 5

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // Storing options as JSON array string: ["A. opt1", "B. opt2", "C. opt3", "D. opt4"]
    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    // "A", "B", "C", or "D"
    @Column(nullable = false)
    private String correctAnswer;

    // Explanation shown after user answers
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // What user selected — null until answered
    private String userAnswer;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession testSession;
}