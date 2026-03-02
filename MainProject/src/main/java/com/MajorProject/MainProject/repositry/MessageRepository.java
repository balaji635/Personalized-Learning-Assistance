package com.MajorProject.MainProject.repositry;

import com.MajorProject.MainProject.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Load all messages for a conversation in chronological order
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    // Count messages in a conversation
    int countByConversationId(Long conversationId);
}