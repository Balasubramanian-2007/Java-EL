package com.example.questionpaper.model;

// Used for POST /api/auth/login
public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {}

    public String getEmail()                 { return email; }
    public void   setEmail(String e)         { this.email = e; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }
}
