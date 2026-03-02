package com.MajorProject.MainProject.repositry;

import com.MajorProject.MainProject.model.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<TestSession, Long> {

    // Fixed: t.scorePercentage (not t.score), and SUBMITTED (not COMPLETED)
    @Query("SELECT AVG(t.scorePercentage) FROM TestSession t WHERE t.user.id = :userId AND t.status = 'SUBMITTED'")
    Double findAverageScoreByUserId(@Param("userId") UUID userId);
}