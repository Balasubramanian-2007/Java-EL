package com.example.questionpaper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuestionPaperApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionPaperApplication.class, args);
        System.out.println("\n QPGen Server running at http://localhost:8080\n");
    }
}
