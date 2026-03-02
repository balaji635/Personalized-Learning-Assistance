package com.MajorProject.MainProject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "correct_option_index", nullable = false)
    private Integer correctOptionIndex; // 0-3 (index of correct option)

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "question_order")
    private Integer questionOrder;

    // What user selected — null until submitted
    @Column(name = "selected_option_index")
    private Integer selectedOptionIndex;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id", nullable = false)
    private TestSession testSession;

    @OneToMany(mappedBy = "testQuestion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("optionIndex ASC")
    private List<QuestionOption> options;
}