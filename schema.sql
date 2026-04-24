-- =====================================================
--  schema.sql
--  Run this ONCE in psql or pgAdmin before starting the app.
--  Command: psql -U postgres -d questionpaper_db -f schema.sql
-- =====================================================

-- Drop tables if they exist (for clean re-runs during development)
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS users;

-- =====================================================
--  USERS TABLE
--  Stores staff and COE accounts
-- =====================================================
CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,       -- BCrypt hash, never plain text
    role          VARCHAR(20)   NOT NULL         -- 'staff' or 'coe'
                  CHECK (role IN ('staff', 'coe')),
    created_at    TIMESTAMP DEFAULT NOW()
);

-- =====================================================
--  QUESTIONS TABLE
--  Now tracks who added the question and approval status
-- =====================================================
CREATE TABLE questions (
    id             SERIAL PRIMARY KEY,
    question_text  TEXT          NOT NULL,
    subject        VARCHAR(100)  NOT NULL,
    topic          VARCHAR(100)  NOT NULL,
    difficulty     VARCHAR(20)   NOT NULL
                   CHECK (difficulty IN ('easy', 'medium', 'hard')),
    marks          INTEGER       NOT NULL
                   CHECK (marks IN (2, 8)),
    type           VARCHAR(20)   NOT NULL
                   CHECK (type IN ('MCQ', 'Descriptive')),
    option_a       VARCHAR(300),
    option_b       VARCHAR(300),
    option_c       VARCHAR(300),
    option_d       VARCHAR(300),
    correct_option CHAR(1),

    -- New columns for role-based workflow
    added_by       INTEGER       REFERENCES users(id),   -- who added it
    approved_by    INTEGER       REFERENCES users(id),   -- who approved it (COE)
    status         VARCHAR(20)   NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending', 'approved', 'rejected')),

    created_at     TIMESTAMP DEFAULT NOW()
);

-- =====================================================
--  SAMPLE DATA
-- =====================================================

-- Sample COE account  (password: coe123)
-- Sample Staff account (password: staff123)
-- These BCrypt hashes are pre-generated for convenience.
-- You can also register through the signup page.

INSERT INTO users (name, email, password_hash, role) VALUES
  ('Dr. Admin COE',  'coe@college.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'coe'),
  ('Prof. Staff',    'staff@college.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'staff');

-- Sample questions (approved, added by COE user id=1)
INSERT INTO questions (question_text, subject, topic, difficulty, marks, type, option_a, option_b, option_c, option_d, correct_option, added_by, approved_by, status)
VALUES
  ('What is the derivative of x^2?', 'Mathematics', 'Calculus', 'easy', 2, 'MCQ', '2x', 'x', 'x^2', '2', 'A', 1, 1, 'approved'),
  ('What is Newton''s Second Law?', 'Physics', 'Mechanics', 'easy', 2, 'MCQ', 'F=ma', 'F=mv', 'F=m/a', 'F=a/m', 'A', 1, 1, 'approved'),
  ('Explain the concept of polymorphism in OOP.', 'Computer Science', 'OOP', 'medium', 8, 'Descriptive', NULL, NULL, NULL, NULL, NULL, 1, 1, 'approved'),
  ('Derive the equation for simple harmonic motion.', 'Physics', 'Oscillations', 'hard', 8, 'Descriptive', NULL, NULL, NULL, NULL, NULL, 1, 1, 'approved'),
  ('What does SQL stand for?', 'Computer Science', 'Databases', 'easy', 2, 'MCQ', 'Structured Query Language', 'Simple Query Language', 'Standard Query Logic', 'System Query Layer', 'A', 1, 1, 'approved'),
  ('Explain the Big-O notation with examples.', 'Computer Science', 'Algorithms', 'medium', 8, 'Descriptive', NULL, NULL, NULL, NULL, NULL, 1, 1, 'approved'),
  ('What is the value of Pi up to 2 decimal places?', 'Mathematics', 'Constants', 'easy', 2, 'MCQ', '3.14', '3.41', '3.12', '3.16', 'A', 1, 1, 'approved'),
  ('Explain Dijkstra''s shortest path algorithm.', 'Computer Science', 'Algorithms', 'hard', 8, 'Descriptive', NULL, NULL, NULL, NULL, NULL, 1, 1, 'approved');
