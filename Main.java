import java.io.*;
import java.util.*;   // Needed for file handling (FileWriter, BufferedWriter, IOException)

/**
 * ============================================================
 *   QUESTION PAPER GENERATOR - Java Experiential Learning
 * ============================================================
 *  OOP Concepts Used:
 *    - Enum
 *    - Abstract Class (Abstraction)
 *    - Inheritance
 *    - Polymorphism
 *    - Composition (Chapter has Questions)
 *    - Interface (loose coupling for storage)
 *    - File Handling (writing output to a .txt file)
 * ============================================================
 */
public class Main{

    // ----------------------------------------------------------------
    // ENUM - Defines the two types of questions and their mark values
    // ----------------------------------------------------------------
    enum QuestionType {
        TWO_MARK(2),
        EIGHT_MARK(8);

        private final int marks;

        QuestionType(int marks) {
            this.marks = marks;
        }

        // Returns the mark value of the question type
        public int getMarks() {
            return marks;
        }
    }

    // ----------------------------------------------------------------
    // ABSTRACT CLASS - A general "Question" blueprint
    // Any specific question type MUST extend this and implement display()
    // ----------------------------------------------------------------
    abstract static class Question {
        private String text;        // The question text
        private QuestionType type;  // Whether it's a 2-mark or 8-mark question

        public Question(String text, QuestionType type) {
            this.text = text;
            this.type = type;
        }

        public QuestionType getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        // ABSTRACTION: Subclasses must define how to display themselves
        public abstract void display();

        // For file writing — returns a formatted string version of the question
        public abstract String toFileString();
    }

    // ----------------------------------------------------------------
    // INHERITANCE - TwoMarkQuestion extends Question
    // ----------------------------------------------------------------
    static class TwoMarkQuestion extends Question {

        public TwoMarkQuestion(String text) {
            super(text, QuestionType.TWO_MARK);
        }

        // POLYMORPHISM - Each subclass has its own version of display()
        @Override
        public void display() {
            System.out.println("  [2 Marks] " + getText());
        }

        @Override
        public String toFileString() {
            return "[2 Marks] " + getText();
        }
    }

    // ----------------------------------------------------------------
    // INHERITANCE - EightMarkQuestion extends Question
    // ----------------------------------------------------------------
    static class EightMarkQuestion extends Question {

        public EightMarkQuestion(String text) {
            super(text, QuestionType.EIGHT_MARK);
        }

        @Override
        public void display() {
            System.out.println("  [8 Marks] " + getText());
        }

        @Override
        public String toFileString() {
            return "[8 Marks] " + getText();
        }
    }

    // ----------------------------------------------------------------
    // COMPOSITION - A Chapter "has" a list of Questions
    // ----------------------------------------------------------------
    static class Chapter {
        private String name;
        private List<Question> questions = new ArrayList<>();

        public Chapter(String name) {
            this.name = name;
        }

        public void addQuestion(Question q) {
            questions.add(q);
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public String getName() {
            return name;
        }
    }

    // ----------------------------------------------------------------
    // COMPOSITION - A Subject "has" a list of Chapters
    // ----------------------------------------------------------------
    static class Subject {
        private String name;
        private List<Chapter> chapters = new ArrayList<>();

        public Subject(String name) {
            this.name = name;
        }

        public void addChapter(Chapter c) {
            chapters.add(c);
        }

        public List<Chapter> getChapters() {
            return chapters;
        }

        public String getName() {
            return name;
        }
    }

    // ----------------------------------------------------------------
    // INTERFACE - Defines what any storage system must be able to do
    // (Loose coupling: we can swap MemoryStorage for DatabaseStorage later)
    // ----------------------------------------------------------------
    interface Storage {
        void saveSubject(Subject subject);
        Subject loadSubject(String name);
    }

    // ----------------------------------------------------------------
    // IN-MEMORY IMPLEMENTATION of the Storage interface
    // Stores subjects in a HashMap (like a temporary database in RAM)
    // ----------------------------------------------------------------
    static class MemoryStorage implements Storage {
        private Map<String, Subject> db = new HashMap<>();

        @Override
        public void saveSubject(Subject subject) {
            db.put(subject.getName(), subject);
        }

        @Override
        public Subject loadSubject(String name) {
            return db.get(name);
        }
    }

    // ----------------------------------------------------------------
    // FILE HANDLER - Handles writing the question paper to a .txt file
    // This demonstrates Java File Handling using FileWriter & BufferedWriter
    // ----------------------------------------------------------------
    static class FileHandler {

        /**
         * Writes the generated question paper to a .txt file.
         *
         * @param subject    - The subject name (used in the file header)
         * @param paper      - The list of selected questions
         * @param totalMarks - The total marks of the paper
         * @param filename   - Name of the output file (e.g., "QuestionPaper.txt")
         */
        public static void writeToFile(Subject subject, List<Question> paper,
                                       int totalMarks, String filename) {

            // Try-with-resources: automatically closes the file after writing
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

                // Write a header section
                writer.write("============================================");
                writer.newLine();
                writer.write("       QUESTION PAPER");
                writer.newLine();
                writer.write("Subject : " + subject.getName());
                writer.newLine();
                writer.write("Total Marks : " + totalMarks);
                writer.newLine();
                writer.write("Total Questions : " + paper.size());
                writer.newLine();
                writer.write("============================================");
                writer.newLine();
                writer.newLine();

                // Write each question
                int qNo = 1;
                for (Question q : paper) {
                    writer.write(qNo + ". " + q.toFileString());
                    writer.newLine();
                    qNo++;
                }

                writer.newLine();
                writer.write("============================================");
                writer.newLine();
                writer.write("        END OF QUESTION PAPER");
                writer.newLine();
                writer.write("============================================");

                // Success message
                System.out.println("\n[FILE SAVED] Question paper written to: " + filename);

            } catch (IOException e) {
                // IOException is thrown if the file cannot be created or written
                System.out.println("[ERROR] Could not write to file: " + e.getMessage());
            }
        }
    }

    // ----------------------------------------------------------------
    // QUESTION PAPER GENERATOR
    // Now works based on TOTAL MARKS instead of total number of questions
    // ----------------------------------------------------------------
    static class QuestionPaperGenerator {

        /**
         * Generates a question paper based on a total mark target.
         *
         * How it works:
         *   - Each chapter gets a percentage of the total marks
         *   - Within that mark budget, 8-mark questions are picked first,
         *     then 2-mark questions fill remaining marks
         *
         * @param subject          - The subject to generate the paper from
         * @param totalMarks       - Total marks the paper should add up to
         * @param chapterWeightage - Map of chapter name → percentage of total marks
         * @return                 - The list of selected questions
         */
        public static List<Question> generate(
                Subject subject,
                int totalMarks,
                Map<String, Integer> chapterWeightage
        ) {

            List<Question> paper = new ArrayList<>();

            for (Chapter chapter : subject.getChapters()) {

                // Calculate how many marks this chapter should contribute
                int percent = chapterWeightage.getOrDefault(chapter.getName(), 0);
                int marksForChapter = (totalMarks * percent) / 100;

                System.out.println("\n  Chapter: " + chapter.getName()
                        + " | Allocated Marks: " + marksForChapter);

                // Separate questions into 8-mark and 2-mark pools
                List<Question> eightMarkPool = new ArrayList<>();
                List<Question> twoMarkPool   = new ArrayList<>();

                for (Question q : chapter.getQuestions()) {
                    if (q.getType() == QuestionType.EIGHT_MARK) {
                        eightMarkPool.add(q);
                    } else {
                        twoMarkPool.add(q);
                    }
                }

                // Shuffle both pools so selection is random each time
                Collections.shuffle(eightMarkPool);
                Collections.shuffle(twoMarkPool);

                int remainingMarks = marksForChapter;

                // First, try to fill with 8-mark questions
                for (Question q : eightMarkPool) {
                    if (remainingMarks >= 8) {
                        paper.add(q);
                        remainingMarks -= 8;
                    }
                }

                // Then, fill leftover marks with 2-mark questions
                for (Question q : twoMarkPool) {
                    if (remainingMarks >= 2) {
                        paper.add(q);
                        remainingMarks -= 2;
                    }
                }

                // Warn if the chapter didn't have enough questions to fill its mark quota
                if (remainingMarks > 0) {
                    System.out.println("  [WARNING] Could not fully fill " + marksForChapter
                            + " marks for chapter \"" + chapter.getName()
                            + "\". Short by " + remainingMarks + " marks.");
                }
            }

            return paper;
        }
    }

    // ================================================================
    //  MAIN METHOD — Entry point of the program
    //  Handles user input, drives the full flow
    // ================================================================
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Storage storage = new MemoryStorage();  // Using in-memory storage

        System.out.println("=========================================");
        System.out.println("     QUESTION PAPER GENERATOR");
        System.out.println("=========================================");

        // --- STEP 1: Enter Subject ---
        System.out.print("\nEnter Subject Name: ");
        String subjectName = sc.nextLine();
        Subject subject = new Subject(subjectName);

        // --- STEP 2: Enter Chapters and Questions ---
        System.out.print("Enter number of chapters: ");
        int chCount = sc.nextInt();
        sc.nextLine(); // Clear leftover newline after nextInt()

        for (int i = 0; i < chCount; i++) {
            System.out.println("\n--- Chapter " + (i + 1) + " ---");

            System.out.print("Chapter name: ");
            String chName = sc.nextLine();
            Chapter chapter = new Chapter(chName);

            System.out.print("Number of questions in this chapter: ");
            int qCount = sc.nextInt();
            sc.nextLine();

            for (int j = 0; j < qCount; j++) {
                System.out.println("  Question " + (j + 1) + ":");
                System.out.print("  Question text: ");
                String text = sc.nextLine();

                System.out.print("  Marks (2 or 8): ");
                int marks = sc.nextInt();
                sc.nextLine();

                // Create the right type of question object based on marks
                if (marks == 2) {
                    chapter.addQuestion(new TwoMarkQuestion(text));
                } else if (marks == 8) {
                    chapter.addQuestion(new EightMarkQuestion(text));
                } else {
                    System.out.println("  [SKIPPED] Only 2 or 8 mark questions allowed.");
                }
            }

            subject.addChapter(chapter);
        }

        // Save the subject to in-memory storage
        storage.saveSubject(subject);
        System.out.println("\n[INFO] Subject \"" + subjectName + "\" saved successfully.");

        // --- STEP 3: Set Total Marks for the Paper ---
        System.out.print("\nEnter TOTAL MARKS for the question paper: ");
        int totalMarks = sc.nextInt();
        sc.nextLine();

        // --- STEP 4: Set Weightage % per Chapter ---
        System.out.println("\nEnter weightage percentage for each chapter");
        System.out.println("(All percentages should ideally add up to 100)");

        Map<String, Integer> weightage = new HashMap<>();
        int totalPercent = 0;

        for (Chapter c : subject.getChapters()) {
            System.out.print("  Weightage % for \"" + c.getName() + "\": ");
            int percent = sc.nextInt();
            weightage.put(c.getName(), percent);
            totalPercent += percent;
        }

        // Warn if percentages don't add to 100
        if (totalPercent != 100) {
            System.out.println("\n[WARNING] Percentages add up to " + totalPercent
                    + "%, not 100%. Paper marks may not match total exactly.");
        }

        // --- STEP 5: Generate the Paper ---
        System.out.println("\n\n=========================================");
        System.out.println("       GENERATED QUESTION PAPER");
        System.out.println(" Subject : " + subject.getName());
        System.out.println(" Total Marks : " + totalMarks);
        System.out.println("=========================================");

        List<Question> paper = QuestionPaperGenerator.generate(subject, totalMarks, weightage);

        // Display the paper on screen
        System.out.println("\n--- Questions ---");
        int qNo = 1;
        int actualMarks = 0;
        for (Question q : paper) {
            System.out.print("  Q" + qNo + ". ");
            q.display(); // POLYMORPHISM in action
            actualMarks += q.getType().getMarks();
            qNo++;
        }

        System.out.println("\nTotal Questions Generated : " + paper.size());
        System.out.println("Total Marks of Paper      : " + actualMarks);

        // --- STEP 6: File Handling — Save to .txt ---
        System.out.print("\nEnter filename to save the paper (e.g., MyPaper.txt): ");
        String filename = sc.nextLine().trim();

        // Use a default name if the user just presses Enter
        if (filename.isEmpty()) {
            filename = "QuestionPaper.txt";
        }

        // Write the paper to the file using our FileHandler class
        FileHandler.writeToFile(subject, paper, actualMarks, filename);

        sc.close();
        System.out.println("\nThank you! Goodbye.");
    }
}