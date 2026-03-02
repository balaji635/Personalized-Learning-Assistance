package com.MajorProject.MainProject.service;

import com.MajorProject.MainProject.model.Conversation;
import com.MajorProject.MainProject.model.Message;
import com.MajorProject.MainProject.model.User;
import com.MajorProject.MainProject.repositry.ConversationRepository;
import com.MajorProject.MainProject.repositry.MessageRepository;
import com.MajorProject.MainProject.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<Conversation> getUserConversations(String email) {
        User user = getUser(email);
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    @Transactional
    public Conversation createConversation(String email, String title, Conversation.DifficultyLevel difficulty) {
        User user = getUser(email);

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(title != null ? title : "New Conversation");
        conversation.setDifficultyLevel(difficulty != null ? difficulty : Conversation.DifficultyLevel.BEGINNER);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    public List<Message> getConversationMessages(String email, Long conversationId) {
        User user = getUser(email);

        if (!conversationRepository.existsByIdAndUserId(conversationId, user.getId())) {
            throw new RuntimeException("Conversation not found or access denied");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void deleteConversation(String email, Long conversationId) {
        User user = getUser(email);

        Conversation conversation = conversationRepository
                .findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found or access denied"));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        messageRepository.deleteAll(messages);
        conversationRepository.delete(conversation);
    }

    @Transactional
    public void updateTitleIfDefault(Long conversationId, String firstMessage) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            if ("New Conversation".equals(conv.getTitle())) {
                String autoTitle = firstMessage.length() > 50
                        ? firstMessage.substring(0, 50) + "..."
                        : firstMessage;
                conv.setTitle(autoTitle);
                conversationRepository.save(conv);
            }
        });
    }

    // ← UUID parameter
    public Conversation getConversationForUser(Long conversationId, UUID userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found or access denied"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}