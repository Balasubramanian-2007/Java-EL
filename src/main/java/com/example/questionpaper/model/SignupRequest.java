package com.example.questionpaper.model;

// Used for POST /api/auth/signup
public class SignupRequest {

    private String name;
    private String email;
    private String password;
    private String role;   // "staff" or "coe"

    public SignupRequest() {}

    public String getName()                  { return name; }
    public void   setName(String n)          { this.name = n; }

    public String getEmail()                 { return email; }
    public void   setEmail(String e)         { this.email = e; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public String getRole()                  { return role; }
    public void   setRole(String r)          { this.role = r; }
}
