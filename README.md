# QPGen — Question Paper Generator

A Spring Boot + PostgreSQL web app to manage a question bank and generate question papers. Includes a role-based system with **Staff** and **COE (Controller of Examination)** roles.

---

## Features

- **Login / Signup** with BCrypt password hashing (no JWT, just sessionStorage)
- **Two roles:**
  - **Staff** — can add questions (goes to pending), view their own submissions + approval status, generate papers
  - **COE** — can add questions (auto-approved), approve/reject staff submissions, delete questions, generate papers
- **Approval workflow** — staff questions show as pending until COE reviews them
- **Paper generation** — picks randomly from approved questions only

---

## Prerequisites

Make sure these are installed before you start:

1. **Java 17** — [Download](https://adoptium.net/)
2. **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
3. **PostgreSQL 14+** — [Download](https://www.postgresql.org/download/)

---

## Step-by-Step Setup

### Step 1 — Create the Database

Open pgAdmin or your terminal and create a new database:

```sql
CREATE DATABASE questionpaper_db;
```

### Step 2 — Run the Schema

In your terminal, run:

```bash
psql -U postgres -d questionpaper_db -f schema.sql
```

Or paste the contents of `schema.sql` into pgAdmin's Query Tool and run it.

This creates the `users` and `questions` tables and inserts sample data including two demo accounts.

### Step 3 — Configure Database Connection

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/questionpaper_db
spring.datasource.username=postgres
spring.datasource.password=yourpassword   ← change this to your PostgreSQL password
```

### Step 4 — Run the Application

In the project root folder (where `pom.xml` is), run:

```bash
mvn spring-boot:run
```

Wait for the message:
```
✅ QPGen Server running at http://localhost:8080
```

### Step 5 — Open in Browser

Go to: **http://localhost:8080/login.html**

---

## Demo Accounts

These accounts are inserted by `schema.sql`:

| Role  | Email                  | Password |
|-------|------------------------|----------|
| COE   | coe@college.com        | coe123   |
| Staff | staff@college.com      | staff123 |

You can also register new accounts from the signup page.

---

## How the Approval Workflow Works

1. **Staff logs in** → goes to Add Question → submits a question
2. Question is saved with status = `pending`
3. **COE logs in** → sees a banner on Dashboard and the "⏳ Approvals" nav link
4. COE clicks Approve → question status becomes `approved`
5. The question now appears in the main Question Bank and can be used for paper generation
6. If COE rejects → status becomes `rejected`, it stays in the staff's "My Submissions" page with the status shown

---

## Project Structure

```
qpgen/
├── pom.xml                          ← Maven build file
├── schema.sql                       ← Run this once to set up the DB
├── README.md
└── src/
    └── main/
        ├── java/com/example/questionpaper/
        │   ├── QuestionPaperApplication.java    ← Main entry point
        │   ├── controller/
        │   │   ├── AuthController.java          ← /api/auth/login, /signup
        │   │   └── QuestionController.java      ← /api/questions, /generate-paper
        │   ├── model/
        │   │   ├── User.java
        │   │   ├── Question.java
        │   │   ├── LoginRequest.java
        │   │   ├── SignupRequest.java
        │   │   └── GeneratePaperRequest.java
        │   ├── repository/
        │   │   ├── UserRepository.java          ← DB queries for users
        │   │   └── QuestionRepository.java      ← DB queries for questions
        │   └── service/
        │       ├── AuthService.java             ← Signup/login logic + BCrypt
        │       └── QuestionService.java         ← Question & paper logic
        └── resources/
            ├── application.properties
            └── static/
                ├── css/style.css
                ├── js/auth.js                   ← Shared session helpers
                ├── login.html
                ├── signup.html
                ├── index.html                   ← Dashboard
                ├── add-question.html
                ├── view-questions.html
                ├── generate-paper.html
                ├── pending-approvals.html       ← COE only
                └── my-questions.html            ← Staff only
```

---

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/signup` | Register a new user |
| POST | `/api/auth/login` | Login, returns user info |
| GET | `/api/questions` | Get all approved questions |
| GET | `/api/questions?subject=Math` | Filter by subject |
| GET | `/api/questions/subjects` | Get distinct subject list |
| GET | `/api/questions/pending` | Get all pending questions |
| GET | `/api/questions/my?userId=5` | Get questions by a specific user |
| POST | `/api/questions?userId=5&userRole=staff` | Add a question |
| PATCH | `/api/questions/{id}/approve` | Approve or reject a question |
| DELETE | `/api/questions/{id}` | Delete a question |
| POST | `/api/generate-paper` | Generate a question paper |

---

## Common Issues

**"Could not connect to server"** — Make sure you ran `mvn spring-boot:run` and the server is on port 8080.

**"Password authentication failed"** — Double-check the password in `application.properties` matches your PostgreSQL password.

**"relation questions does not exist"** — You haven't run `schema.sql` yet. See Step 2.

**Port 8080 already in use** — Change `server.port=8080` to `server.port=8081` in `application.properties`, then access the app at `http://localhost:8081`.
