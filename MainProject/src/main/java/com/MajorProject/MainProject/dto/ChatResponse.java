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
public class ChatResponse {
    private Long messageId;
    private String role;
    private String content;
    private Long conversationId;
    private LocalDateTime timestamp;
}