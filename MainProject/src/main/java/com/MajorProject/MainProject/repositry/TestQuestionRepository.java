package com.MajorProject.MainProject.repositry;

import com.MajorProject.MainProject.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    List<TestQuestion> findByTestSessionIdOrderByQuestionOrderAsc(Long testSessionId);
}