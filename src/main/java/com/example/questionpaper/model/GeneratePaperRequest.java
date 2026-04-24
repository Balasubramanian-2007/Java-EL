package com.example.questionpaper.model;

public class GeneratePaperRequest {

    private String subject;
    private int    totalMarks;
    private int    numShortQuestions;
    private int    numLongQuestions;
    private String difficulty;

    public GeneratePaperRequest() {}

    public String getSubject()                   { return subject; }
    public void   setSubject(String s)           { this.subject = s; }

    public int  getTotalMarks()                  { return totalMarks; }
    public void setTotalMarks(int t)             { this.totalMarks = t; }

    public int  getNumShortQuestions()           { return numShortQuestions; }
    public void setNumShortQuestions(int n)      { this.numShortQuestions = n; }

    public int  getNumLongQuestions()            { return numLongQuestions; }
    public void setNumLongQuestions(int n)       { this.numLongQuestions = n; }

    public String getDifficulty()                { return difficulty; }
    public void   setDifficulty(String d)        { this.difficulty = d; }
}
