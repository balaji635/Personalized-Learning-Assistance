package com.MajorProject.MainProject.service;

import com.MajorProject.MainProject.dto.GenerateTestRequest;
import com.MajorProject.MainProject.dto.SubmitTestRequest;
import com.MajorProject.MainProject.model.*;
import com.MajorProject.MainProject.repositry.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {

    private final ChatClient chatClient;
    private final TestSessionRepository testSessionRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ----------------------------------------
    // GENERATE TEST
    // ----------------------------------------
    @Transactional
    public TestSession generateTest(String email, GenerateTestRequest request) {

        User user = getUser(email);

        // If documentId provided, get relevant context from PgVector
        String documentContext = "";
        Document sourceDocument = null;

        if (request.getDocumentId() != null) {
            sourceDocument = documentRepository.findById(request.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            if (!sourceDocument.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied to document");
            }

            documentContext = documentService.searchRelevantContext(
                    request.getTopic(),
                    user.getId().toString(),
                    8  // get more chunks for test generation
            );
        }

        // Build prompt for MCQ generation
        String prompt = buildMcqPrompt(request, documentContext);

        // Call AI to generate MCQs
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.debug("AI MCQ response: {}", aiResponse);

        // Parse AI response into structured questions
        List<TestQuestion> questions = parseAiResponse(aiResponse, request.getQuestionCount());

        if (questions.isEmpty()) {
            throw new RuntimeException("Failed to generate valid questions. Please try again.");
        }

        // Save TestSession
        TestSession session = TestSession.builder()
                .user(user)
                .topic(request.getTopic())
                .difficultyLevel(Conversation.DifficultyLevel.valueOf(
                        request.getDifficultyLevel().toUpperCase()))
                .status(TestSession.TestStatus.GENERATED)
                .totalQuestions(questions.size())
                .sourceDocument(sourceDocument)
                .build();
        session = testSessionRepository.save(session);

        // Save questions and options
        for (int i = 0; i < questions.size(); i++) {
            TestQuestion q = questions.get(i);
            q.setTestSession(session);
            q.setQuestionOrder(i + 1);

            List<QuestionOption> options = q.getOptions();
            q.setOptions(null);  // detach options temporarily
            TestQuestion savedQ = testQuestionRepository.save(q);  // gets real DB id

            // Now attach options to saved question id
            if (options != null) {
                for (QuestionOption opt : options) {
                    opt.setTestQuestion(savedQ);
                }
                savedQ.setOptions(options);
                testQuestionRepository.save(savedQ);  // cascade saves options with question_id
            }
            savedQ.getOptions().forEach(opt -> opt.setTestQuestion(savedQ));
        }

        // Reload with questions
        return testSessionRepository.findById(session.getId()).orElse(session);
    }

    // ----------------------------------------
    // SUBMIT TEST
    // ----------------------------------------
    @Transactional
    public TestSession submitTest(String email, Long sessionId, SubmitTestRequest request) {

        User user = getUser(email);

        TestSession session = testSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Test not found or access denied"));

        if (session.getStatus() == TestSession.TestStatus.SUBMITTED) {
            throw new RuntimeException("Test already submitted");
        }

        List<TestQuestion> questions = testQuestionRepository
                .findByTestSessionIdOrderByQuestionOrderAsc(sessionId);

        int correct = 0;

        for (TestQuestion question : questions) {
            Integer selectedIndex = request.getAnswers().get(question.getId());
            if (selectedIndex != null) {
                question.setSelectedOptionIndex(selectedIndex);
                if (selectedIndex.equals(question.getCorrectOptionIndex())) {
                    correct++;
                }
                testQuestionRepository.save(question);
            }
        }

        // Update session with results
        double percentage = questions.isEmpty() ? 0 :
                Math.round((correct * 100.0 / questions.size()) * 100.0) / 100.0;

        session.setCorrectAnswers(correct);
        session.setScorePercentage(percentage);
        session.setStatus(TestSession.TestStatus.SUBMITTED);
        session.setSubmittedAt(LocalDateTime.now());

        return testSessionRepository.save(session);
    }

    // ----------------------------------------
    // GET USER TEST HISTORY
    // ----------------------------------------
    public List<TestSession> getUserTests(String email) {
        User user = getUser(email);
        return testSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // ----------------------------------------
    // GET TEST WITH RESULTS
    // ----------------------------------------
    public TestSession getTestResults(String email, Long sessionId) {
        User user = getUser(email);
        return testSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Test not found or access denied"));
    }

    // ----------------------------------------
    // BUILD MCQ PROMPT
    // ----------------------------------------
    private String buildMcqPrompt(GenerateTestRequest request, String documentContext) {
        StringBuilder prompt = new StringBuilder();

        if (!documentContext.isBlank()) {
            prompt.append("Based on the following content:\n\n")
                    .append(documentContext)
                    .append("\n\n");
        }

        prompt.append(String.format("""
                Generate exactly %d multiple choice questions about: "%s"
                Difficulty level: %s
                
                IMPORTANT: Respond with ONLY a valid JSON array. No explanation, no markdown, no extra text.
                
                Format:
                [
                  {
                    "question": "Question text here?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctOptionIndex": 0,
                    "explanation": "Brief explanation of why this answer is correct"
                  }
                ]
                
                Rules:
                - exacty %d questions
                - exactly 4 options each
                - correctOptionIndex is 0, 1, 2, or 3
                - questions should test understanding, not just memorization
                - return ONLY the JSON array, nothing else
                """,
                request.getQuestionCount(),
                request.getTopic(),
                request.getDifficultyLevel(),
                request.getQuestionCount()
        ));

        return prompt.toString();
    }

    // ----------------------------------------
    // PARSE AI JSON RESPONSE
    // ----------------------------------------
    private List<TestQuestion> parseAiResponse(String aiResponse, int expectedCount) {
        List<TestQuestion> questions = new ArrayList<>();

        try {
            // Strip markdown code blocks if present
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            }

            // Find JSON array in response
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start == -1 || end == -1) {
                log.error("No JSON array found in AI response: {}", aiResponse);
                return questions;
            }

            cleaned = cleaned.substring(start, end + 1);
            JsonNode jsonArray = objectMapper.readTree(cleaned);

            for (JsonNode node : jsonArray) {
                TestQuestion question = new TestQuestion();
                question.setQuestion(node.get("question").asText());
                question.setCorrectOptionIndex(node.get("correctOptionIndex").asInt());
                question.setExplanation(
                        node.has("explanation") ? node.get("explanation").asText() : ""
                );

                // Parse options
                List<QuestionOption> options = new ArrayList<>();
                JsonNode optionsNode = node.get("options");
                for (int i = 0; i < optionsNode.size(); i++) {
                    QuestionOption opt = QuestionOption.builder()
                            .optionIndex(i)
                            .optionText(optionsNode.get(i).asText())
                            .build();
                    options.add(opt);
                }
                question.setOptions(options);
                questions.add(question);
            }

        } catch (Exception e) {
            log.error("Failed to parse AI MCQ response: {}", e.getMessage());
        }

        return questions;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}