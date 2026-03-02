package com.MajorProject.MainProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
}