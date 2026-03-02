package com.MajorProject.MainProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private String title;
    private String difficultyLevel;
    private int messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}