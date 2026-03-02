package com.MajorProject.MainProject.service;

import com.MajorProject.MainProject.config.JpaChatMemory;
import com.MajorProject.MainProject.dto.ChatRequest;
import com.MajorProject.MainProject.dto.ChatResponse;
import com.MajorProject.MainProject.model.Conversation;
import com.MajorProject.MainProject.model.Message;
import com.MajorProject.MainProject.model.User;
import com.MajorProject.MainProject.repositry.MessageRepository;
import com.MajorProject.MainProject.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final JpaChatMemory jpaChatMemory;
    private final DocumentService documentService; // ← RAG

    @Transactional
    public ChatResponse chat(String email, Long conversationId, ChatRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Conversation conversation = conversationService.getConversationForUser(conversationId, user.getId());

        String memoryKey = "user_" + user.getId().toString() + "_conv_" + conversationId;

        // RAG — search user's uploaded documents for relevant context
        String ragContext = documentService.searchRelevantContext(
                request.getMessage(),
                user.getId().toString(),
                6  // top 6 most relevant chunks
        );

        String systemPrompt = buildSystemPrompt(
                conversation.getDifficultyLevel().name(),
                user.getFirstName(),
                ragContext  // inject document context into system prompt
        );

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(jpaChatMemory)
                .conversationId(memoryKey)
                .build();

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(request.getMessage())
                .advisors(memoryAdvisor)
                .call()
                .content();

        conversationService.updateTitleIfDefault(conversationId, request.getMessage());

        Message latestAssistant = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(m -> m.getRole() == Message.MessageRole.ASSISTANT)
                .reduce((first, second) -> second)
                .orElse(null);

        log.info("Chat completed for conversation {} user {}", conversationId, email);

        return ChatResponse.builder()
                .messageId(latestAssistant != null ? latestAssistant.getId() : null)
                .role("ASSISTANT")
                .content(aiResponse)
                .conversationId(conversationId)
                .timestamp(latestAssistant != null ? latestAssistant.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    public void clearMemory(UUID userId, Long conversationId) {
        jpaChatMemory.clear("user_" + userId.toString() + "_conv_" + conversationId);
    }

    private String buildSystemPrompt(String difficultyLevel, String firstName, String ragContext) {
        String basePrompt = String.format(
                "You are an expert AI learning assistant helping %s. " +
                        "You are knowledgeable, patient, and encouraging. " +
                        "Always provide clear explanations with examples. " +
                        "Remember the context of this conversation. ",
                firstName
        );

        // If relevant document context found, prepend it
        if (ragContext != null && !ragContext.isBlank()) {
            basePrompt = ragContext + "\n\nUsing the above context when relevant, " + basePrompt;
        }

        return switch (difficultyLevel) {
            case "BEGINNER" -> basePrompt +
                    "The student is a BEGINNER. Use simple language, avoid jargon, " +
                    "explain every concept from scratch, use lots of analogies and simple examples.";
            case "INTERMEDIATE" -> basePrompt +
                    "The student is at INTERMEDIATE level. Use proper technical terms, " +
                    "provide code examples where relevant.";
            case "ADVANCED" -> basePrompt +
                    "The student is ADVANCED. Use technical terminology freely, " +
                    "go deep into implementation details, discuss tradeoffs and best practices.";
            default -> basePrompt;
        };
    }
}