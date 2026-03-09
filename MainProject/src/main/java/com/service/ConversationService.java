package com.service;

import com.exception.NotFoundException;
import com.model.Conversation;
import com.model.Message;
import com.model.User;
import com.repositry.ConversationRepository;
import com.repositry.MessageRepository;
import com.repositry.UserRepository;
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
        conversation.setTitle(normalizeTitle(title));
        conversation.setDifficultyLevel(difficulty != null ? difficulty : Conversation.DifficultyLevel.BEGINNER);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    public List<Message> getConversationMessages(String email, Long conversationId) {
        User user = getUser(email);

        if (!conversationRepository.existsByIdAndUserId(conversationId, user.getId())) {
            throw new NotFoundException("Conversation not found");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void deleteConversation(String email, Long conversationId) {
        User user = getUser(email);

        Conversation conversation = conversationRepository
                .findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        messageRepository.deleteAll(messages);
        conversationRepository.delete(conversation);
    }

    @Transactional
    public void updateTitleIfDefault(Long conversationId, String firstMessage) {
        if (firstMessage == null || firstMessage.isBlank()) {
            return;
        }

        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if ("New Conversation".equals(conversation.getTitle())) {
                String cleanedMessage = firstMessage.trim();
                String autoTitle = cleanedMessage.length() > 50
                        ? cleanedMessage.substring(0, 50) + "..."
                        : cleanedMessage;
                conversation.setTitle(autoTitle);
                conversationRepository.save(conversation);
            }
        });
    }

    public Conversation getConversationForUser(Long conversationId, UUID userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "New Conversation";
        }
        return title.trim();
    }
}
