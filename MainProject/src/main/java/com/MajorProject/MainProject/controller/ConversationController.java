package com.MajorProject.MainProject.controller;

import com.MajorProject.MainProject.dto.ApiResponse;
import com.MajorProject.MainProject.model.Conversation;
import com.MajorProject.MainProject.model.Message;
import com.MajorProject.MainProject.service.ConversationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // ----------------------------------------
    // GET all conversations for current user
    // GET /api/conversations
    // ----------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Conversation> conversations = conversationService
                .getUserConversations(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Conversations retrieved", conversations));
    }

    // ----------------------------------------
    // CREATE new conversation
    // POST /api/conversations
    // Body: { "title": "My Java Session", "difficultyLevel": "BEGINNER" }
    // ----------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Conversation>> createConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreateConversationRequest request) {

        Conversation conversation = conversationService.createConversation(
                userDetails.getUsername(),
                request.getTitle(),
                request.getDifficultyLevel()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created", conversation));
    }

    // ----------------------------------------
    // GET messages for a conversation
    // GET /api/conversations/{id}/messages
    // ----------------------------------------
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        List<Message> messages = conversationService
                .getConversationMessages(userDetails.getUsername(), id);

        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", messages));
    }

    // ----------------------------------------
    // DELETE a conversation
    // DELETE /api/conversations/{id}
    // ----------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        conversationService.deleteConversation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }

    // ----------------------------------------
    // REQUEST DTO (inner class for simplicity)
    // ----------------------------------------
    @Data
    public static class CreateConversationRequest {
        private String title;

        @NotNull
        private Conversation.DifficultyLevel difficultyLevel = Conversation.DifficultyLevel.BEGINNER;
    }
}