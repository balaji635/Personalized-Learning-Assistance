package com.repositry;

import com.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);  // ← UUID

    Optional<Conversation> findByIdAndUserId(Long id, UUID userId);    // ← UUID

    boolean existsByIdAndUserId(Long id, UUID userId);                 // ← UUID
}