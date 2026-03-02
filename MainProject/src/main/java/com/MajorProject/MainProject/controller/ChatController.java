package com.MajorProject.MainProject.controller;

import com.MajorProject.MainProject.dto.ApiResponse;
import com.MajorProject.MainProject.dto.ChatRequest;   // ← use the dto, NOT inner class
import com.MajorProject.MainProject.dto.ChatResponse;  // ← use the dto
import com.MajorProject.MainProject.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/chat/{conversationId}
     * Authorization: Bearer {token}
     * Body: { "message": "Explain Java streams" }
     */
    @PostMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @RequestBody @Valid ChatRequest request) {

        // Pass the full ChatRequest object — NOT request.getMessage()
        ChatResponse response = chatService.chat(
                userDetails.getUsername(),
                conversationId,
                request  // ← full DTO, not String
        );

        return ResponseEntity.ok(ApiResponse.success("Response generated", response));
    }
}