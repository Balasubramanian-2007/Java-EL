// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
   public Main() {
   }

   public static void main(String[] var0) {
      Scanner var1 = new Scanner(System.in);
      MemoryStorage var2 = new MemoryStorage();
      System.out.println("=========================================");
      System.out.println("     QUESTION PAPER GENERATOR");
      System.out.println("=========================================");
      System.out.print("\nEnter Subject Name: ");
      String var3 = var1.nextLine();
      Subject var4 = new Subject(var3);
      System.out.print("Enter number of chapters: ");
      int var5 = var1.nextInt();
      var1.nextLine();

      for(int var6 = 0; var6 < var5; ++var6) {
         System.out.println("\n--- Chapter " + (var6 + 1) + " ---");
         System.out.print("Chapter name: ");
         String var7 = var1.nextLine();
         Chapter var8 = new Chapter(var7);
         System.out.print("Number of questions in this chapter: ");
         int var9 = var1.nextInt();
         var1.nextLine();

         for(int var10 = 0; var10 < var9; ++var10) {
            System.out.println("  Question " + (var10 + 1) + ":");
            System.out.print("  Question text: ");
            String var11 = var1.nextLine();
            System.out.print("  Marks (2 or 8): ");
            int var12 = var1.nextInt();
            var1.nextLine();
            if (var12 == 2) {
               var8.addQuestion(new TwoMarkQuestion(var11));
            } else if (var12 == 8) {
               var8.addQuestion(new EightMarkQuestion(var11));
            } else {
               System.out.println("  [SKIPPED] Only 2 or 8 mark questions allowed.");
            }
         }

         var4.addChapter(var8);
      }

      var2.saveSubject(var4);
      System.out.println("\n[INFO] Subject \"" + var3 + "\" saved successfully.");
      System.out.print("\nEnter TOTAL MARKS for the question paper: ");
      int var14 = var1.nextInt();
      var1.nextLine();
      System.out.println("\nEnter weightage percentage for each chapter");
      System.out.println("(All percentages should ideally add up to 100)");
      HashMap var15 = new HashMap();
      int var16 = 0;

      for(Chapter var19 : var4.getChapters()) {
         System.out.print("  Weightage % for \"" + var19.getName() + "\": ");
         int var21 = var1.nextInt();
         var15.put(var19.getName(), var21);
         var16 += var21;
      }

      if (var16 != 100) {
         System.out.println("\n[WARNING] Percentages add up to " + var16 + "%, not 100%. Paper marks may not match total exactly.");
      }

      System.out.println("\n\n=========================================");
      System.out.println("       GENERATED QUESTION PAPER");
      System.out.println(" Subject : " + var4.getName());
      System.out.println(" Total Marks : " + var14);
      System.out.println("=========================================");
      List var18 = Main.QuestionPaperGenerator.generate(var4, var14, var15);
      System.out.println("\n--- Questions ---");
      int var20 = 1;
      int var22 = 0;

      for(Question var13 : var18) {
         System.out.print("  Q" + var20 + ". ");
         var13.display();
         var22 += var13.getType().getMarks();
         ++var20;
      }

      System.out.println("\nTotal Questions Generated : " + var18.size());
      System.out.println("Total Marks of Paper      : " + var22);
      System.out.print("\nEnter filename to save the paper (e.g., MyPaper.txt): ");
      String var24 = var1.nextLine().trim();
      if (var24.isEmpty()) {
         var24 = "QuestionPaper.txt";
      }

      Main.FileHandler.writeToFile(var4, var18, var22, var24);
      var1.close();
      System.out.println("\nThank you! Goodbye.");
   }

   static enum QuestionType {
      TWO_MARK(2),
      EIGHT_MARK(8);

      private final int marks;

      private QuestionType(int var3) {
         this.marks = var3;
      }

      public int getMarks() {
         return this.marks;
      }
   }

   abstract static class Question {
      private String text;
      private QuestionType type;

      public Question(String var1, QuestionType var2) {
         this.text = var1;
         this.type = var2;
      }

      public QuestionType getType() {
         return this.type;
      }

      public String getText() {
         return this.text;
      }

      public abstract void display();

      public abstract String toFileString();
   }

   static class TwoMarkQuestion extends Question {
      public TwoMarkQuestion(String var1) {
         super(var1, Main.QuestionType.TWO_MARK);
      }

      public void display() {
         System.out.println("  [2 Marks] " + this.getText());
      }

      public String toFileString() {
         return "[2 Marks] " + this.getText();
      }
   }

   static class EightMarkQuestion extends Question {
      public EightMarkQuestion(String var1) {
         super(var1, Main.QuestionType.EIGHT_MARK);
      }

      public void display() {
         System.out.println("  [8 Marks] " + this.getText());
      }

      public String toFileString() {
         return "[8 Marks] " + this.getText();
      }
   }

   static class Chapter {
      private String name;
      private List<Question> questions = new ArrayList();

      public Chapter(String var1) {
         this.name = var1;
      }

      public void addQuestion(Question var1) {
         this.questions.add(var1);
      }

      public List<Question> getQuestions() {
         return this.questions;
      }

      public String getName() {
         return this.name;
      }
   }

   static class Subject {
      private String name;
      private List<Chapter> chapters = new ArrayList();

      public Subject(String var1) {
         this.name = var1;
      }

      public void addChapter(Chapter var1) {
         this.chapters.add(var1);
      }

      public List<Chapter> getChapters() {
         return this.chapters;
      }

      public String getName() {
         return this.name;
      }
   }

   static class MemoryStorage implements Storage {
      private Map<String, Subject> db = new HashMap();

      MemoryStorage() {
      }

      public void saveSubject(Subject var1) {
         this.db.put(var1.getName(), var1);
      }

      public Subject loadSubject(String var1) {
         return (Subject)this.db.get(var1);
      }
   }

   static class FileHandler {
      FileHandler() {
      }

      public static void writeToFile(Subject var0, List<Question> var1, int var2, String var3) {
         try {
            BufferedWriter var4 = new BufferedWriter(new FileWriter(var3));

            try {
               var4.write("============================================");
               var4.newLine();
               var4.write("       QUESTION PAPER");
               var4.newLine();
               var4.write("Subject : " + var0.getName());
               var4.newLine();
               var4.write("Total Marks : " + var2);
               var4.newLine();
               var4.write("Total Questions : " + var1.size());
               var4.newLine();
               var4.write("============================================");
               var4.newLine();
               var4.newLine();
               int var5 = 1;

               for(Question var7 : var1) {
                  var4.write(var5 + ". " + var7.toFileString());
                  var4.newLine();
                  ++var5;
               }

               var4.newLine();
               var4.write("============================================");
               var4.newLine();
               var4.write("        END OF QUESTION PAPER");
               var4.newLine();
               var4.write("============================================");
               System.out.println("\n[FILE SAVED] Question paper written to: " + var3);
            } catch (Throwable var9) {
               try {
                  var4.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            var4.close();
         } catch (IOException var10) {
            System.out.println("[ERROR] Could not write to file: " + var10.getMessage());
         }

      }
   }

   static class QuestionPaperGenerator {
      QuestionPaperGenerator() {
      }

      public static List<Question> generate(Subject var0, int var1, Map<String, Integer> var2) {
         ArrayList var3 = new ArrayList();

         for(Chapter var5 : var0.getChapters()) {
            int var6 = (Integer)var2.getOrDefault(var5.getName(), 0);
            int var7 = var1 * var6 / 100;
            PrintStream var10000 = System.out;
            String var10001 = var5.getName();
            var10000.println("\n  Chapter: " + var10001 + " | Allocated Marks: " + var7);
            ArrayList var8 = new ArrayList();
            ArrayList var9 = new ArrayList();

            for(Question var11 : var5.getQuestions()) {
               if (var11.getType() == Main.QuestionType.EIGHT_MARK) {
                  var8.add(var11);
               } else {
                  var9.add(var11);
               }
            }

            Collections.shuffle(var8);
            Collections.shuffle(var9);
            int var13 = var7;

            for(Question var12 : var8) {
               if (var13 >= 8) {
                  var3.add(var12);
                  var13 -= 8;
               }
            }

            for(Question var16 : var9) {
               if (var13 >= 2) {
                  var3.add(var16);
                  var13 -= 2;
               }
            }

            if (var13 > 0) {
               System.out.println("  [WARNING] Could not fully fill " + var7 + " marks for chapter \"" + var5.getName() + "\". Short by " + var13 + " marks.");
            }
         }

         return var3;
      }
   }

   interface Storage {
      void saveSubject(Subject var1);

      Subject loadSubject(String var1);
   }
}
