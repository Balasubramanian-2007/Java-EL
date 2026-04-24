package com.example.questionpaper.service;

import com.example.questionpaper.model.GeneratePaperRequest;
import com.example.questionpaper.model.Question;
import com.example.questionpaper.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    // Get all approved questions (optionally filtered by subject)
    public List<Question> getAllQuestions(String subject) {
        return questionRepository.findAllApproved(subject);
    }

    // Get questions added by a specific user (staff sees own submissions)
    public List<Question> getQuestionsByUser(Long userId) {
        return questionRepository.findByUser(userId);
    }

    // Get all pending questions (COE use)
    public List<Question> getPendingQuestions() {
        return questionRepository.findAllPending();
    }

    // Get distinct subjects list
    public List<String> getAllSubjects() {
        return questionRepository.findAllSubjects();
    }

    // Add a question.
    // If the user is COE → auto-approved.
    // If the user is staff → status is "pending".
    public void addQuestion(Question question, Long userId, String userRole) {
        // Basic validation
        if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
            throw new IllegalArgumentException("Question text cannot be empty.");
        }
        if (question.getSubject() == null || question.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject cannot be empty.");
        }
        if (question.getMarks() != 2 && question.getMarks() != 8) {
            throw new IllegalArgumentException("Marks must be 2 or 8.");
        }

        question.setAddedBy(userId);

        if ("coe".equals(userRole)) {
            // COE questions are automatically approved
            question.setStatus("approved");
            question.setApprovedBy(userId);
        } else {
            // Staff questions need COE approval
            question.setStatus("pending");
            question.setApprovedBy(null);
        }

        questionRepository.save(question);
    }

    // Approve or reject a question (COE only)
    public void updateApprovalStatus(Long questionId, String status, Long coeUserId) {
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new IllegalArgumentException("Status must be 'approved' or 'rejected'.");
        }
        questionRepository.updateStatus(questionId, status, coeUserId);
    }

    // Delete a question by ID
    public boolean deleteQuestion(Long id) {
        return questionRepository.deleteById(id) > 0;
    }

    // Generate a question paper (picks only approved questions)
    public List<Question> generatePaper(GeneratePaperRequest request) {
        List<Question> paper = new ArrayList<>();

        if (request.getNumShortQuestions() > 0) {
            List<Question> shortQs = questionRepository.findForPaper(
                    request.getSubject(), 2,
                    request.getDifficulty(),
                    request.getNumShortQuestions()
            );
            paper.addAll(shortQs);
        }

        if (request.getNumLongQuestions() > 0) {
            List<Question> longQs = questionRepository.findForPaper(
                    request.getSubject(), 8,
                    request.getDifficulty(),
                    request.getNumLongQuestions()
            );
            paper.addAll(longQs);
        }

        int actualMarks = paper.stream().mapToInt(Question::getMarks).sum();
        System.out.println("Paper: " + paper.size() + " questions, " + actualMarks + " marks");

        return paper;
    }
}
