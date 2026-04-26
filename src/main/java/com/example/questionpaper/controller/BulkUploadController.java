package com.example.questionpaper.controller;

import com.example.questionpaper.model.Question;
import com.example.questionpaper.service.QuestionService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * BulkUploadController.java
 *
 * Handles POST /api/questions/bulk
 * The frontend sends a .xlsx file (multipart/form-data).
 * We read it row by row using Apache POI and save each question.
 *
 * Expected Excel columns (row 1 = header, data starts row 2):
 *   question_text | subject | topic | difficulty | marks | type |
 *   option_a | option_b | option_c | option_d | correct_option
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class BulkUploadController {

    private final QuestionService questionService;

    public BulkUploadController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // POST /api/questions/bulk?userId=5&userRole=staff
    @PostMapping("/bulk")
    public ResponseEntity<String> bulkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam String userRole) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body("Only .xlsx files are supported.");
        }

        List<String> errors = new ArrayList<>();
        int savedCount      = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // DataFormatter converts any cell type (string, numeric, formula)
            // to a readable string — this avoids the blank-cell bug caused
            // by setCellType() on already-typed cells.
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                // Skip header row
                if (row.getRowNum() == 0) continue;

                // Skip completely empty rows
                if (isRowEmpty(row, formatter)) continue;

                int rowNumber = row.getRowNum() + 1; // 1-based for error messages

                try {
                    Question q = parseRow(row, formatter);
                    questionService.addQuestion(q, userId, userRole);
                    savedCount++;
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to read Excel file: " + e.getMessage());
        }

        // Build summary response
        if (savedCount == 0) {
            return ResponseEntity.badRequest()
                    .body("No questions were saved.\n\n" + String.join("\n", errors));
        }

        StringBuilder msg = new StringBuilder();
        msg.append(savedCount).append(" question(s) uploaded successfully.");
        if ("staff".equals(userRole)) {
            msg.append(" They are pending COE approval.");
        }
        if (!errors.isEmpty()) {
            msg.append("\n\nSkipped rows due to errors:\n");
            errors.forEach(e -> msg.append("  • ").append(e).append("\n"));
        }

        return ResponseEntity.ok(msg.toString());
    }

    // ----------------------------------------------------------------
    //  Parse one row into a Question object.
    //  Uses DataFormatter to safely read every cell as a String.
    // ----------------------------------------------------------------
    private Question parseRow(Row row, DataFormatter formatter) {
        Question q = new Question();

        q.setQuestionText(getString(row, 0, formatter));
        q.setSubject(getString(row, 1, formatter));
        q.setTopic(getString(row, 2, formatter));
        q.setDifficulty(getString(row, 3, formatter).toLowerCase());
        q.setMarks(getInt(row, 4, formatter));
        q.setType(getString(row, 5, formatter));
        q.setOptionA(getStringOrNull(row, 6, formatter));
        q.setOptionB(getStringOrNull(row, 7, formatter));
        q.setOptionC(getStringOrNull(row, 8, formatter));
        q.setOptionD(getStringOrNull(row, 9, formatter));
        q.setCorrectOption(getStringOrNull(row, 10, formatter));

        // Validate
        if (q.getQuestionText().isBlank())
            throw new IllegalArgumentException("question_text is empty.");
        if (q.getSubject().isBlank())
            throw new IllegalArgumentException("subject is empty.");
        if (q.getTopic().isBlank())
            throw new IllegalArgumentException("topic is empty.");
        if (!List.of("easy", "medium", "hard").contains(q.getDifficulty()))
            throw new IllegalArgumentException("difficulty must be easy/medium/hard. Got: \"" + q.getDifficulty() + "\"");
        if (q.getMarks() != 2 && q.getMarks() != 8)
            throw new IllegalArgumentException("marks must be 2 or 8. Got: " + q.getMarks());
        if (!List.of("MCQ", "Descriptive").contains(q.getType()))
            throw new IllegalArgumentException("type must be MCQ or Descriptive. Got: \"" + q.getType() + "\"");

        return q;
    }

    // ---- Cell reading helpers ----

    // Returns cell value as a trimmed String (never null)
    private String getString(Row row, int col, DataFormatter fmt) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return fmt.formatCellValue(cell).trim();
    }

    // Returns trimmed String or null if empty (for optional MCQ fields)
    private String getStringOrNull(Row row, int col, DataFormatter fmt) {
        String val = getString(row, col, fmt);
        return val.isEmpty() ? null : val;
    }

    // Returns int value — handles cells stored as numbers or as text like "2"
    private int getInt(Row row, int col, DataFormatter fmt) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return 0;
        // If it's a numeric cell, read directly to avoid "2.0" string formatting
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        try {
            return Integer.parseInt(fmt.formatCellValue(cell).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Returns true if all columns A-K are blank
    private boolean isRowEmpty(Row row, DataFormatter fmt) {
        for (int i = 0; i <= 10; i++) {
            if (!getString(row, i, fmt).isEmpty()) return false;
        }
        return true;
    }
}