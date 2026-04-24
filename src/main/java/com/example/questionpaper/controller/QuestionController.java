package com.example.questionpaper.controller;

import com.example.questionpaper.model.GeneratePaperRequest;
import com.example.questionpaper.model.Question;
import com.example.questionpaper.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // GET /api/questions?subject=Mathematics
    // Returns all APPROVED questions (main question bank view)
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(required = false) String subject) {
        return ResponseEntity.ok(questionService.getAllQuestions(subject));
    }

    // GET /api/questions/my?userId=5
    // Returns all questions added by a specific user (any status)
    // Staff uses this to track their submissions
    @GetMapping("/questions/my")
    public ResponseEntity<List<Question>> getMyQuestions(@RequestParam Long userId) {
        return ResponseEntity.ok(questionService.getQuestionsByUser(userId));
    }

    // GET /api/questions/pending
    // Returns all questions with status = 'pending' (COE review page)
    @GetMapping("/questions/pending")
    public ResponseEntity<List<Question>> getPendingQuestions() {
        return ResponseEntity.ok(questionService.getPendingQuestions());
    }

    // GET /api/questions/subjects
    // Returns distinct subject names from approved questions
    @GetMapping("/questions/subjects")
    public ResponseEntity<List<String>> getAllSubjects() {
        return ResponseEntity.ok(questionService.getAllSubjects());
    }

    // POST /api/questions?userId=5&userRole=staff
    // Adds a question. Status depends on role.
    @PostMapping("/questions")
    public ResponseEntity<String> addQuestion(
            @RequestBody Question question,
            @RequestParam Long userId,
            @RequestParam String userRole) {
        try {
            questionService.addQuestion(question, userId, userRole);
            if ("coe".equals(userRole)) {
                return ResponseEntity.status(201).body("Question added and auto-approved!");
            } else {
                return ResponseEntity.status(201).body("Question submitted for COE approval.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/questions/{id}/approve
    // Body: { status: "approved" or "rejected", coeUserId: 1 }
    // Only COE should call this (enforced on frontend)
    @PatchMapping("/questions/{id}/approve")
    public ResponseEntity<String> approveQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String status   = (String) body.get("status");
            Long coeUserId  = Long.valueOf(body.get("coeUserId").toString());
            questionService.updateApprovalStatus(id, status, coeUserId);
            return ResponseEntity.ok("Question " + status + " successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/questions/{id}
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        boolean deleted = questionService.deleteQuestion(id);
        if (deleted) {
            return ResponseEntity.ok("Question deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Question not found with ID: " + id);
        }
    }

    // POST /api/generate-paper
    @PostMapping("/generate-paper")
    public ResponseEntity<?> generatePaper(@RequestBody GeneratePaperRequest request) {
        try {
            List<Question> paper = questionService.generatePaper(request);
            if (paper.isEmpty()) {
                return ResponseEntity.status(404)
                        .body("No approved questions found for your criteria. Try different filters.");
            }
            return ResponseEntity.ok(paper);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
