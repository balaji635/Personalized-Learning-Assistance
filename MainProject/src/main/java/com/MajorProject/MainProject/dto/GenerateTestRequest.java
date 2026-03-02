package com.MajorProject.MainProject.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateTestRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    @Min(3) @Max(20)
    private int questionCount = 5; // default 5 questions

    private String difficultyLevel = "INTERMEDIATE";

    // Optional — if provided, generate from document content (RAG)
    private Long documentId;
}