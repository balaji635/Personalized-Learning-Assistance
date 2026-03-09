package com.service;

import com.config.JpaChatMemory;
import com.dto.ChatRequest;
import com.dto.ChatResponse;
import com.exception.BadRequestException;
import com.exception.NotFoundException;
import com.model.Conversation;
import com.model.Message;
import com.model.User;
import com.repositry.MessageRepository;
import com.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
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
    private final DocumentService documentService;

    @Transactional
    public ChatResponse chat(String email, Long conversationId, ChatRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Conversation conversation = conversationService.getConversationForUser(conversationId, user.getId());
        String userMessage = request.getMessage().trim();
        String memoryKey = "user_" + user.getId() + "_conv_" + conversationId;
        boolean strictDocumentMode = isStrictDocumentRequest(userMessage);

        String ragContext = documentService.searchRelevantContext(
                userMessage,
                user.getId().toString(),
                10
        );

        String systemPrompt = buildSystemPrompt(
                conversation.getDifficultyLevel().name(),
                user.getFirstName(),
                ragContext,
                strictDocumentMode
        );

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(jpaChatMemory)
                .conversationId(memoryKey)
                .build();

        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .advisors(memoryAdvisor)
                .call()
                .content();

        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BadRequestException("AI returned an empty response");
        }

        conversationService.updateTitleIfDefault(conversationId, userMessage);

        Message latestAssistant = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(message -> message.getRole() == Message.MessageRole.ASSISTANT)
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
        jpaChatMemory.clear("user_" + userId + "_conv_" + conversationId);
    }

    private String buildSystemPrompt(String difficultyLevel, String firstName, String ragContext, boolean strictDocumentMode) {
        String groundingRules = "Grounding rules:\n" +
                "1) Use the retrieved document context as the primary source when it is provided.\n" +
                "2) Never invent page numbers, section numbers, quotes, or facts not present in retrieved context.\n" +
                "3) If a user asks about a specific page but page data is not present in context, say that page-level evidence is unavailable.\n" +
                "4) If an earlier assistant response may be wrong, acknowledge uncertainty and correct only using available evidence.\n" +
                "5) When possible, mention source labels like file/page from retrieved context.\n";

        String confidenceStyle = "Confidence style:\n" +
                "1) Be confident, direct, and clear when evidence supports an answer.\n" +
                "2) Do not be overconfident when evidence is missing. Clearly state limits instead of guessing.\n";

        String strictModeRules = "";
        if (strictDocumentMode) {
            strictModeRules = "STRICT DOCUMENT MODE ENABLED (user asked for strict PDF/document answer):\n" +
                    "1) Answer using ONLY the retrieved document context in this turn.\n" +
                    "2) Do NOT use general knowledge, prior guesses, or unstated assumptions for factual claims.\n" +
                    "3) If requested detail is not present in retrieved context, explicitly say it is not available in retrieved document context.\n" +
                    "4) Never fabricate page numbers or section labels.\n";
        }

        String basePrompt = String.format(
                "You are an expert AI learning assistant helping %s. " +
                        "You are knowledgeable, patient, and encouraging. " +
                        "Always provide clear explanations with examples. " +
                        "Remember the context of this conversation. ",
                firstName
        );

        StringBuilder contextualPrompt = new StringBuilder();
        contextualPrompt.append(groundingRules).append("\n");
        contextualPrompt.append(confidenceStyle).append("\n");

        if (!strictModeRules.isBlank()) {
            contextualPrompt.append(strictModeRules).append("\n");
        }

        if (ragContext != null && !ragContext.isBlank()) {
            contextualPrompt.append("Retrieved context:\n").append(ragContext).append("\n");
        } else if (strictDocumentMode) {
            contextualPrompt.append("Retrieved context:\n[No matching document context found for this query]\n");
        }

        String mergedPrompt = contextualPrompt + "\n" + basePrompt;

        return switch (difficultyLevel) {
            case "BEGINNER" -> mergedPrompt +
                    "The student is a BEGINNER. Use simple language, avoid jargon, " +
                    "explain every concept from scratch, and use clear examples.";
            case "INTERMEDIATE" -> mergedPrompt +
                    "The student is at INTERMEDIATE level. Use proper technical terms " +
                    "and provide examples where relevant.";
            case "ADVANCED" -> mergedPrompt +
                    "The student is ADVANCED. Use technical terminology freely, " +
                    "go deep into implementation details, and discuss tradeoffs.";
            default -> mergedPrompt;
        };
    }

    private boolean isStrictDocumentRequest(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }

        String normalized = userMessage.toLowerCase(Locale.ROOT);

        boolean mentionsDocument = normalized.contains("document") || normalized.contains("pdf") || normalized.contains("file");
        boolean asksStrict = normalized.contains("strict") || normalized.contains("only") || normalized.contains("exact") || normalized.contains("just");
        boolean asksFromSource = normalized.contains("from document") || normalized.contains("from pdf")
                || normalized.contains("from the document") || normalized.contains("from the pdf")
                || normalized.contains("document only") || normalized.contains("pdf only");

        return (mentionsDocument && asksStrict) || asksFromSource;
    }
}
