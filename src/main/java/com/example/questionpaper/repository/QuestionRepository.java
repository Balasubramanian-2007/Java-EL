package com.example.questionpaper.repository;

import com.example.questionpaper.model.Question;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class QuestionRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Maps a database row to a Question object.
    // We LEFT JOIN users twice — once for the adder, once for the approver.
    private final RowMapper<Question> questionRowMapper = new RowMapper<Question>() {
        @Override
        public Question mapRow(ResultSet rs, int rowNum) throws SQLException {
            Question q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setSubject(rs.getString("subject"));
            q.setTopic(rs.getString("topic"));
            q.setDifficulty(rs.getString("difficulty"));
            q.setMarks(rs.getInt("marks"));
            q.setType(rs.getString("type"));
            q.setOptionA(rs.getString("option_a"));
            q.setOptionB(rs.getString("option_b"));
            q.setOptionC(rs.getString("option_c"));
            q.setOptionD(rs.getString("option_d"));
            q.setCorrectOption(rs.getString("correct_option"));
            q.setAddedBy(rs.getLong("added_by"));
            q.setAddedByName(rs.getString("added_by_name"));
            q.setApprovedBy(rs.getLong("approved_by"));
            q.setApprovedByName(rs.getString("approved_by_name"));
            q.setStatus(rs.getString("status"));
            return q;
        }
    };

    // Base SELECT with JOINs to get user names
    private static final String SELECT_BASE = """
            SELECT q.*,
                   u1.name AS added_by_name,
                   u2.name AS approved_by_name
            FROM questions q
            LEFT JOIN users u1 ON q.added_by    = u1.id
            LEFT JOIN users u2 ON q.approved_by = u2.id
            """;

    // Get all approved questions (optionally filtered by subject)
    // Used for the main question bank view and paper generation
    public List<Question> findAllApproved(String subject) {
        if (subject != null && !subject.isEmpty()) {
            String sql = SELECT_BASE + " WHERE q.status = 'approved' AND q.subject = ? ORDER BY q.id";
            return jdbcTemplate.query(sql, questionRowMapper, subject);
        } else {
            String sql = SELECT_BASE + " WHERE q.status = 'approved' ORDER BY q.id";
            return jdbcTemplate.query(sql, questionRowMapper);
        }
    }

    // Get questions added by a specific staff user (all statuses)
    // Used so staff can see their own submissions and check status
    public List<Question> findByUser(Long userId) {
        String sql = SELECT_BASE + " WHERE q.added_by = ? ORDER BY q.id DESC";
        return jdbcTemplate.query(sql, questionRowMapper, userId);
    }

    // Get all pending questions — used by COE to review them
    public List<Question> findAllPending() {
        String sql = SELECT_BASE + " WHERE q.status = 'pending' ORDER BY q.id DESC";
        return jdbcTemplate.query(sql, questionRowMapper);
    }

    // Get distinct subjects (only from approved questions, for dropdowns)
    public List<String> findAllSubjects() {
        String sql = "SELECT DISTINCT subject FROM questions WHERE status = 'approved' ORDER BY subject";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // Save a new question
    public void save(Question q) {
        String sql = """
                INSERT INTO questions
                  (question_text, subject, topic, difficulty, marks, type,
                   option_a, option_b, option_c, option_d, correct_option,
                   added_by, approved_by, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                q.getQuestionText(), q.getSubject(), q.getTopic(),
                q.getDifficulty(), q.getMarks(), q.getType(),
                q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                q.getCorrectOption(), q.getAddedBy(), q.getApprovedBy(), q.getStatus()
        );
    }

    // Delete by ID — returns number of rows deleted
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM questions WHERE id = ?", id);
    }

    // Update approval status of a question
    public void updateStatus(Long questionId, String status, Long approvedBy) {
        String sql = "UPDATE questions SET status = ?, approved_by = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, approvedBy, questionId);
    }

    // Find questions for paper generation (only approved ones)
    public List<Question> findForPaper(String subject, int marks, String difficulty, int limit) {
        if (difficulty != null && !difficulty.equals("any")) {
            String sql = SELECT_BASE + """
                    WHERE q.status = 'approved' AND q.subject = ? AND q.marks = ? AND q.difficulty = ?
                    ORDER BY RANDOM()
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, questionRowMapper, subject, marks, difficulty, limit);
        } else {
            String sql = SELECT_BASE + """
                    WHERE q.status = 'approved' AND q.subject = ? AND q.marks = ?
                    ORDER BY RANDOM()
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, questionRowMapper, subject, marks, limit);
        }
    }
}
