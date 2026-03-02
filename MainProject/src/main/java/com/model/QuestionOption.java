package com.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_index", nullable = false)
    private Integer optionIndex; // 0, 1, 2, 3

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;

    // ----------------------------------------
    // Relationships
    // ----------------------------------------
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion testQuestion;
}