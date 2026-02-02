import java.util.*;

/*
 * SINGLE FILE OOP-RICH QUESTION PAPER GENERATOR
 * Console based
 */

public class QuestionPaperApp {

    // ---------- ENUM ----------
    enum QuestionType {
        TWO_MARK(2),
        EIGHT_MARK(8);

        private final int marks;

        QuestionType(int marks) {
            this.marks = marks;
        }

        public int getMarks() {
            return marks;
        }
    }

    // ---------- ABSTRACT CLASS (ABSTRACTION) ----------
    abstract static class Question {
        private String text;
        private QuestionType type;

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

        // POLYMORPHISM
        public abstract void display();
    }

    // ---------- INHERITANCE ----------
    static class TwoMarkQuestion extends Question {
        public TwoMarkQuestion(String text) {
            super(text, QuestionType.TWO_MARK);
        }

        @Override
        public void display() {
            System.out.println("[2 Marks] " + getText());
        }
    }

    static class EightMarkQuestion extends Question {
        public EightMarkQuestion(String text) {
            super(text, QuestionType.EIGHT_MARK);
        }

        @Override
        public void display() {
            System.out.println("[8 Marks] " + getText());
        }
    }

    // ---------- CHAPTER (COMPOSITION) ----------
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

    // ---------- SUBJECT ----------
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

    // ---------- INTERFACE (LOOSE COUPLING) ----------
    interface Storage {
        void saveSubject(Subject subject);
        Subject loadSubject(String name);
    }

    // ---------- IN-MEMORY IMPLEMENTATION ----------
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

    // ---------- QUESTION PAPER GENERATOR ----------
    static class QuestionPaperGenerator {

        public static void generate(
                Subject subject,
                int totalQuestions,
                Map<String, Integer> chapterWeightage
        ) {
            List<Question> paper = new ArrayList<>();

            for (Chapter chapter : subject.getChapters()) {
                int percent = chapterWeightage.getOrDefault(chapter.getName(), 0);
                int count = (totalQuestions * percent) / 100;

                List<Question> pool = new ArrayList<>(chapter.getQuestions());
                Collections.shuffle(pool);

                for (int i = 0; i < Math.min(count, pool.size()); i++) {
                    paper.add(pool.get(i));
                }
            }

            System.out.println("\n----- GENERATED QUESTION PAPER -----");
            for (Question q : paper) {
                q.display(); // POLYMORPHISM
            }
            System.out.println("Total Questions Generated: " + paper.size());
        }
    }

    // ---------- MAIN (MENU + FLOW) ----------
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Storage storage = new MemoryStorage();

        System.out.print("Enter Subject Name: ");
        String subjectName = sc.nextLine();
        Subject subject = new Subject(subjectName);

        System.out.print("Enter number of chapters: ");
        int chCount = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < chCount; i++) {
            System.out.print("Chapter name: ");
            String chName = sc.nextLine();
            Chapter chapter = new Chapter(chName);

            System.out.print("Number of questions in this chapter: ");
            int qCount = sc.nextInt();
            sc.nextLine();

            for (int j = 0; j < qCount; j++) {
                System.out.print("Question text: ");
                String text = sc.nextLine();

                System.out.print("Marks (2 or 8): ");
                int marks = sc.nextInt();
                sc.nextLine();

                if (marks == 2) {
                    chapter.addQuestion(new TwoMarkQuestion(text));
                } else {
                    chapter.addQuestion(new EightMarkQuestion(text));
                }
            }

            subject.addChapter(chapter);
        }

        storage.saveSubject(subject);

        // GENERATION PHASE
        System.out.print("\nEnter total questions to generate: ");
        int total = sc.nextInt();
        sc.nextLine();

        Map<String, Integer> weightage = new HashMap<>();
        for (Chapter c : subject.getChapters()) {
            System.out.print("Weightage % for " + c.getName() + ": ");
            weightage.put(c.getName(), sc.nextInt());
        }

        QuestionPaperGenerator.generate(subject, total, weightage);
        sc.close();
    }
}
