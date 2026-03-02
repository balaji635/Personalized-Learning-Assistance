package com.MajorProject.MainProject.config;

import com.MajorProject.MainProject.model.Conversation;
import com.MajorProject.MainProject.model.Message;
import com.MajorProject.MainProject.repositry.ConversationRepository;
import com.MajorProject.MainProject.repositry.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaChatMemory implements ChatMemory {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    // ----------------------------------------
    // ADD messages to DB
    // ----------------------------------------
    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        Long convId = extractConversationId(conversationId);
        if (convId == null) return;

        Conversation conversation = conversationRepository.findById(convId).orElse(null);
        if (conversation == null) return;

        for (org.springframework.ai.chat.messages.Message aiMessage : messages) {
            Message entity = new Message();
            entity.setConversation(conversation);
            entity.setContent(aiMessage.getText());
            entity.setCreatedAt(LocalDateTime.now());

            if (aiMessage instanceof UserMessage) {
                entity.setRole(Message.MessageRole.USER);
            } else if (aiMessage instanceof AssistantMessage) {
                entity.setRole(Message.MessageRole.ASSISTANT);
            } else {
                continue; // skip system messages
            }

            messageRepository.save(entity);
        }

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    // ----------------------------------------
    // GET all messages from DB
    // Spring AI 2.0.0-M2 — no lastN param, returns ALL messages
    // ----------------------------------------
    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId) {
        Long convId = extractConversationId(conversationId);
        if (convId == null) return List.of();

        List<Message> dbMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(convId);

        // Limit to last 20 messages to avoid huge context windows
        int size = dbMessages.size();
        if (size > 20) {
            dbMessages = dbMessages.subList(size - 20, size);
        }

        return dbMessages.stream()
                .map(this::toAiMessage)
                .collect(Collectors.toList());
    }

    // ----------------------------------------
    // CLEAR all messages for a conversation
    // ----------------------------------------
    @Override
    public void clear(String conversationId) {
        Long convId = extractConversationId(conversationId);
        if (convId == null) return;

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(convId);
        messageRepository.deleteAll(messages);
        log.debug("Cleared {} messages for conversation {}", messages.size(), conversationId);
    }

    // ----------------------------------------
    // HELPERS
    // ----------------------------------------
    private Long extractConversationId(String conversationId) {
        try {
            // Format: "user_1_conv_5"
            String[] parts = conversationId.split("_conv_");
            if (parts.length == 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (Exception e) {
            log.warn("Could not parse conversationId: {}", conversationId);
        }
        return null;
    }

    private org.springframework.ai.chat.messages.Message toAiMessage(Message entity) {
        if (entity.getRole() == Message.MessageRole.USER) {
            return new UserMessage(entity.getContent());
        } else {
            return new AssistantMessage(entity.getContent());
        }
    }
}