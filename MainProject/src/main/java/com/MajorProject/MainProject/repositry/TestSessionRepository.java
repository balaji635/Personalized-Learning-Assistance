package com.MajorProject.MainProject.repositry;

import com.MajorProject.MainProject.model.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<TestSession> findByIdAndUserId(Long id, UUID userId);
}