package com.example.questionpaper.model;

public class Question {

    private Long   id;
    private String questionText;
    private String subject;
    private String topic;
    private String difficulty;
    private int    marks;
    private String type;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;

    // Role-based workflow fields
    private Long   addedBy;         // user id of who added it
    private String addedByName;     // user name (for display)
    private Long   approvedBy;      // user id of COE who approved
    private String approvedByName;  // COE name (for display)
    private String status;          // "pending", "approved", "rejected"

    public Question() {}

    // Getters and Setters

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getQuestionText()             { return questionText; }
    public void setQuestionText(String t)       { this.questionText = t; }

    public String getSubject()                  { return subject; }
    public void setSubject(String s)            { this.subject = s; }

    public String getTopic()                    { return topic; }
    public void setTopic(String t)              { this.topic = t; }

    public String getDifficulty()               { return difficulty; }
    public void setDifficulty(String d)         { this.difficulty = d; }

    public int getMarks()                       { return marks; }
    public void setMarks(int m)                 { this.marks = m; }

    public String getType()                     { return type; }
    public void setType(String t)               { this.type = t; }

    public String getOptionA()                  { return optionA; }
    public void setOptionA(String o)            { this.optionA = o; }

    public String getOptionB()                  { return optionB; }
    public void setOptionB(String o)            { this.optionB = o; }

    public String getOptionC()                  { return optionC; }
    public void setOptionC(String o)            { this.optionC = o; }

    public String getOptionD()                  { return optionD; }
    public void setOptionD(String o)            { this.optionD = o; }

    public String getCorrectOption()            { return correctOption; }
    public void setCorrectOption(String o)      { this.correctOption = o; }

    public Long getAddedBy()                    { return addedBy; }
    public void setAddedBy(Long u)              { this.addedBy = u; }

    public String getAddedByName()              { return addedByName; }
    public void setAddedByName(String n)        { this.addedByName = n; }

    public Long getApprovedBy()                 { return approvedBy; }
    public void setApprovedBy(Long u)           { this.approvedBy = u; }

    public String getApprovedByName()           { return approvedByName; }
    public void setApprovedByName(String n)     { this.approvedByName = n; }

    public String getStatus()                   { return status; }
    public void setStatus(String s)             { this.status = s; }
}
