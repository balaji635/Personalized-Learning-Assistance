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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private static final Set<String> STOP_WORDS = Set.of(
            "the","a","an","is","it","in","on","at","to","for","of","and","or","but",
            "with","this","that","are","was","were","be","been","have","has","had",
            "do","does","did","will","would","can","could","should","may","might",
            "from","by","as","into","about","after","before","its","their","our",
            "your","my","we","he","she","they","i","not","no","so","if","then",
            "than","also","more","how","what","which","give","tell","explain","define"
    );

    @Transactional
    public ChatResponse chat(String email, Long conversationId, ChatRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Conversation conversation = conversationService.getConversationForUser(conversationId, user.getId());
        String userMessage = request.getMessage().trim();
        String memoryKey   = "user_" + user.getId() + "_conv_" + conversationId;

        // ── RAG RETRIEVAL ─────────────────────────────────────────────
        String ragContext = documentService.searchRelevantContext(
                userMessage, user.getId().toString(), 8);
        boolean ragUsed = ragContext != null && !ragContext.isBlank();

        // cosine similarity = 1 - distance (already converted in DocumentService)
        double avgRetrievalScore = documentService.getLastAvgRetrievalScore();

        double relevanceScore    = 0.0;
        double faithfulnessScore = 0.0;

        if (ragUsed) {

            // ════════════════════════════════════════════════════════
            // METRIC 1 — RELEVANCE  (pure math, no LLM)
            // Jaccard Similarity = |Q ∩ C| / |Q ∪ C|
            // How many of the question's key words appear in context?
            // ════════════════════════════════════════════════════════
            relevanceScore = avgRetrievalScore;

            log.info("╔══ RAG RELEVANCE SCORE ══════════════════════════╗");
            log.info("║ Source    : PgVector cosine similarity (1-distance)");
            log.info("║ Question  : {}", userMessage);
            log.info("║ Score     : {}", String.format("%.4f", relevanceScore));
//            log.info("║ Quality   : {}", relevanceScore >= 0.75 ? "GOOD ✅" :
//                    relevanceScore >= 0.55 ? "MODERATE ⚠️" : "POOR ❌");
            log.info("╚═════════════════════════════════════════════════╝");

            // ── BUILD PROMPT + CALL AI ────────────────────────────────
            String systemPrompt = buildSystemPrompt(
                    conversation.getDifficultyLevel().name(),
                    user.getFirstName(),
                    ragContext
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

            // ════════════════════════════════════════════════════════
            // METRIC 2 — FAITHFULNESS  (pure math, no LLM)
            // Token Overlap = |Answer ∩ Context| / |Answer tokens|
            // How many of the answer's key words came from the context?
            // ════════════════════════════════════════════════════════
            faithfulnessScore = tokenOverlap(aiResponse, ragContext);

            log.info("╔══ RAG FAITHFULNESS SCORE (Token Overlap) ═══════╗");
            log.info("║ Formula   : |Answer∩Context| / |Answer tokens|");
            log.info("║ Score     : {}", String.format("%.4f", faithfulnessScore));
//            log.info("║ Hallucination : {}", faithfulnessScore >= 0.55 ? "LOW RISK ✅" :
//                    faithfulnessScore >= 0.30 ? "MODERATE ⚠️" : "HIGH RISK ❌");
            log.info("╚═════════════════════════════════════════════════╝");

            // ════════════════════════════════════════════════════════
            // METRIC 3 — HALLUCINATION RISK  (combines all 3 scores)
            // ════════════════════════════════════════════════════════
            boolean retrievalPoor    = avgRetrievalScore < 0.45;
            boolean faithfulnessPoor = faithfulnessScore < 0.25;


            String finalRisk;
            if (retrievalPoor && faithfulnessPoor) {
                finalRisk = "HIGH ❌  — Answer likely hallucinated!";
            } else if (retrievalPoor) {
                finalRisk = "MEDIUM ⚠️ — Low context match, verify answer.";
            } else {
                finalRisk = "LOW ✅  — Answer grounded in document.";
            }

            log.info("╔══ HALLUCINATION RISK ASSESSMENT ════════════════╗");
            log.info("║ Retrieval Score    : {} (cosine similarity 1-distance)", String.format("%.4f", avgRetrievalScore));
            log.info("║ Relevance Score    : {} (same as retrieval - PgVector cosine)",             String.format("%.4f", relevanceScore));
            log.info("║ Faithfulness Score : {} (token overlap Answer∩Context)", String.format("%.4f", faithfulnessScore));
            log.info("║ FINAL RISK         : {}", finalRisk);
            log.info("╚═════════════════════════════════════════════════╝");

            log.info("╔══ RAG PIPELINE SUMMARY ═════════════════════════╗");
            log.info("║ User     : {}", email);
            log.info("║ RAG Used : YES ✅");
            log.info("╚═════════════════════════════════════════════════╝");

            conversationService.updateTitleIfDefault(conversationId, userMessage);

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

        // ── NO RAG — answer from general knowledge ────────────────────
        String systemPrompt = buildSystemPrompt(
                conversation.getDifficultyLevel().name(),
                user.getFirstName(),
                null
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

        log.info("╔══ RAG PIPELINE SUMMARY ═════════════════════════╗");
        log.info("║ User     : {}", email);
        log.info("║ RAG Used : NO — no documents uploaded");
        log.info("╚═════════════════════════════════════════════════╝");

        conversationService.updateTitleIfDefault(conversationId, userMessage);

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
        jpaChatMemory.clear("user_" + userId + "_conv_" + conversationId);
    }

    // ═════════════════════════════════════════════════════════════
    // MATH — no LLM involved at all
    // ═════════════════════════════════════════════════════════════

    /**
     * Jaccard Similarity = |A ∩ B| / |A ∪ B|
     */
    private double queryRecall(String question, String context) {
        Set<String> questionTokens = tokenize(question);
        Set<String> contextTokens  = tokenize(context);
        if (questionTokens.isEmpty()) return 0.0;

        long matched = questionTokens.stream()
                .filter(contextTokens::contains)
                .count();

        return Math.round((double) matched / questionTokens.size() * 10000.0) / 10000.0;
    }

    /**
     * Token Overlap = |Answer ∩ Context| / |Answer tokens|
     */
    private double tokenOverlap(String answer, String context) {
        Set<String> answerTokens  = tokenize(answer);
        Set<String> contextTokens = tokenize(context);
        if (answerTokens.isEmpty()) return 0.0;

        long overlap = answerTokens.stream()
                .filter(contextTokens::contains)
                .count();

        return Math.round((double) overlap / answerTokens.size() * 10000.0) / 10000.0;
    }

    /**
     * Tokenize: lowercase, remove punctuation, remove stop words, min length 3
     */
    private Set<String> tokenize(String text) {
        return Arrays.stream(
                        text.toLowerCase()
                                .replaceAll("[^a-z0-9\\s]", " ")
                                .trim()
                                .split("\\s+"))
                .filter(t -> t.length() >= 3)
                .filter(t -> !STOP_WORDS.contains(t))
                .collect(Collectors.toSet());
    }

    // ═════════════════════════════════════════════════════════════
    // SYSTEM PROMPT
    // ═════════════════════════════════════════════════════════════
    private String buildSystemPrompt(String difficultyLevel, String firstName, String ragContext) {
        String basePrompt = String.format(
                "You are an AI learning assistant helping %s study technical and academic topics. " +
                        "Your ONLY purpose is to help with learning. \n\n" +
                        "ALLOWED: " +
                        "- Greetings and simple conversational openers (hi, hello, how are you → respond briefly and guide back to learning) " +
                        "- Questions about programming, computer science, mathematics, engineering, science, " +
                        "  history, geography, economics, medicine, law, and any academic subject. " +
                        "- Requests to explain concepts, solve problems, review notes, generate quizzes. \n\n" +
                        "NOT ALLOWED — respond with exactly: " +
                        "\"I'm your learning assistant! I'm here to help you study and understand academic topics. " +
                        "Feel free to ask me anything related to your studies!\" " +
                        "- Emotional support, venting, personal problems, relationships " +
                        "- Movies, celebrities, entertainment, sports gossip, social media " +
                        "- Opinions on politics, religion, or controversial topics " +
                        "- Anything unrelated to learning or academics \n\n" +
                        "Always provide clear explanations with examples when appropriate. " +
                        "Be encouraging and supportive of the student's learning journey. ",
                firstName
        );

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