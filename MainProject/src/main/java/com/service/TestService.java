package com.service;

import com.dto.GenerateTestRequest;
import com.dto.SubmitTestRequest;
import com.exception.BadRequestException;
import com.exception.ConflictException;
import com.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Conversation;
import com.model.Document;
import com.model.QuestionOption;
import com.model.TestQuestion;
import com.model.TestSession;
import com.model.User;
import com.repositry.TestQuestionRepository;
import com.repositry.TestSessionRepository;
import com.repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {

    private final ChatClient chatClient;
    private final TestSessionRepository testSessionRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TestSession generateTest(String email, GenerateTestRequest request) {
        User user = getUser(email);

        String documentContext = "";
        Document sourceDocument = null;

        if (request.getDocumentId() != null) {
            sourceDocument = documentService.getDocumentForUser(email, request.getDocumentId());
            documentContext = documentService.searchRelevantContext(
                    request.getTopic(),
                    user.getId().toString(),
                    8
            );
        }

        String prompt = buildMcqPrompt(request, documentContext);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        List<TestQuestion> questions = parseAiResponse(aiResponse, request.getQuestionCount());
        if (questions.isEmpty()) {
            throw new BadRequestException("Failed to generate valid questions. Please try again.");
        }

        Conversation.DifficultyLevel difficultyLevel = parseDifficultyLevel(request.getDifficultyLevel());

        TestSession session = TestSession.builder()
                .user(user)
                .topic(request.getTopic())
                .difficultyLevel(difficultyLevel)
                .status(TestSession.TestStatus.GENERATED)
                .totalQuestions(questions.size())
                .sourceDocument(sourceDocument)
                .build();
        session = testSessionRepository.save(session);

        for (int i = 0; i < questions.size(); i++) {
            TestQuestion question = questions.get(i);
            question.setTestSession(session);
            question.setQuestionOrder(i + 1);

            List<QuestionOption> options = question.getOptions();
            question.setOptions(null);
            TestQuestion savedQuestion = testQuestionRepository.save(question);

            if (options != null) {
                for (QuestionOption option : options) {
                    option.setTestQuestion(savedQuestion);
                }
                savedQuestion.setOptions(options);
                testQuestionRepository.save(savedQuestion);
            }
        }

        return testSessionRepository.findById(session.getId()).orElse(session);
    }

    @Transactional
    public TestSession submitTest(String email, Long sessionId, SubmitTestRequest request) {
        User user = getUser(email);

        TestSession session = testSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new NotFoundException("Test not found"));

        if (session.getStatus() == TestSession.TestStatus.SUBMITTED) {
            throw new ConflictException("Test already submitted");
        }

        if (request == null || request.getAnswers() == null) {
            throw new BadRequestException("Answers are required");
        }

        List<TestQuestion> questions = testQuestionRepository.findByTestSessionIdOrderByQuestionOrderAsc(sessionId);
        Map<Long, Integer> answers = request.getAnswers();

        Set<Long> questionIds = new HashSet<>();
        for (TestQuestion question : questions) {
            questionIds.add(question.getId());
        }

        for (Long submittedQuestionId : answers.keySet()) {
            if (!questionIds.contains(submittedQuestionId)) {
                throw new BadRequestException("Invalid question id in answers: " + submittedQuestionId);
            }
        }

        int correct = 0;
        for (TestQuestion question : questions) {
            if (!answers.containsKey(question.getId())) {
                continue;
            }

            Integer selectedIndex = answers.get(question.getId());
            if (selectedIndex == null || selectedIndex < 0 || selectedIndex > 3) {
                throw new BadRequestException("Invalid option index for question " + question.getId());
            }

            question.setSelectedOptionIndex(selectedIndex);
            if (selectedIndex.equals(question.getCorrectOptionIndex())) {
                correct++;
            }
            testQuestionRepository.save(question);
        }

        double percentage = questions.isEmpty()
                ? 0
                : Math.round((correct * 100.0 / questions.size()) * 100.0) / 100.0;

        session.setCorrectAnswers(correct);
        session.setScorePercentage(percentage);
        session.setStatus(TestSession.TestStatus.SUBMITTED);
        session.setSubmittedAt(LocalDateTime.now());

        return testSessionRepository.save(session);
    }

    public List<TestSession> getUserTests(String email) {
        User user = getUser(email);
        return testSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public TestSession getTestResults(String email, Long sessionId) {
        User user = getUser(email);
        return testSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new NotFoundException("Test not found"));
    }

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
                - exactly %d questions
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

    private List<TestQuestion> parseAiResponse(String aiResponse, int expectedCount) {
        List<TestQuestion> questions = new ArrayList<>();

        try {
            String cleaned = aiResponse == null ? "" : aiResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\n?", "")
                        .replaceAll("```\\n?", "")
                        .trim();
            }

            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start == -1 || end == -1 || end <= start) {
                log.error("No JSON array found in AI response");
                return questions;
            }

            JsonNode jsonArray = objectMapper.readTree(cleaned.substring(start, end + 1));
            if (!jsonArray.isArray()) {
                return questions;
            }

            for (JsonNode node : jsonArray) {
                if (questions.size() >= expectedCount) {
                    break;
                }

                String questionText = node.path("question").asText("").trim();
                JsonNode optionsNode = node.path("options");
                int correctOptionIndex = node.path("correctOptionIndex").asInt(-1);

                if (questionText.isBlank() || !optionsNode.isArray() || optionsNode.size() != 4) {
                    continue;
                }

                if (correctOptionIndex < 0 || correctOptionIndex > 3) {
                    continue;
                }

                List<QuestionOption> options = new ArrayList<>(4);
                boolean hasBlankOption = false;
                for (int i = 0; i < 4; i++) {
                    String optionText = optionsNode.get(i).asText("").trim();
                    if (optionText.isBlank()) {
                        hasBlankOption = true;
                        break;
                    }

                    options.add(QuestionOption.builder()
                            .optionIndex(i)
                            .optionText(optionText)
                            .build());
                }

                if (hasBlankOption) {
                    continue;
                }

                TestQuestion question = new TestQuestion();
                question.setQuestion(questionText);
                question.setCorrectOptionIndex(correctOptionIndex);
                question.setExplanation(node.path("explanation").asText(""));
                question.setOptions(options);

                questions.add(question);
            }

        } catch (Exception e) {
            log.error("Failed to parse AI MCQ response: {}", e.getMessage());
        }

        return questions;
    }

    private Conversation.DifficultyLevel parseDifficultyLevel(String rawDifficulty) {
        if (rawDifficulty == null || rawDifficulty.isBlank()) {
            return Conversation.DifficultyLevel.INTERMEDIATE;
        }

        try {
            return Conversation.DifficultyLevel.valueOf(rawDifficulty.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid difficulty level. Allowed: BEGINNER, INTERMEDIATE, ADVANCED");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
