package com.example.questionpaper.service;

import com.example.questionpaper.model.LoginRequest;
import com.example.questionpaper.model.SignupRequest;
import com.example.questionpaper.model.User;
import com.example.questionpaper.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;

    // BCryptPasswordEncoder handles hashing and checking passwords
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---- SIGNUP ----
    // Returns a map with either "error" or "message" key
    public Map<String, Object> signup(SignupRequest req) {
        Map<String, Object> result = new HashMap<>();

        // Basic validation
        if (req.getName() == null || req.getName().isBlank()) {
            result.put("error", "Name is required.");
            return result;
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            result.put("error", "Email is required.");
            return result;
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            result.put("error", "Password must be at least 6 characters.");
            return result;
        }
        if (!"staff".equals(req.getRole()) && !"coe".equals(req.getRole())) {
            result.put("error", "Role must be 'staff' or 'coe'.");
            return result;
        }

        // Check if email already taken
        if (userRepository.emailExists(req.getEmail())) {
            result.put("error", "An account with this email already exists.");
            return result;
        }

        // Hash the password before saving (never store plain text)
        String hash = passwordEncoder.encode(req.getPassword());
        userRepository.save(req.getName(), req.getEmail(), hash, req.getRole());

        result.put("message", "Account created successfully! You can now log in.");
        return result;
    }

    // ---- LOGIN ----
    // Returns user info (without password) if login is successful
    public Map<String, Object> login(LoginRequest req) {
        Map<String, Object> result = new HashMap<>();

        if (req.getEmail() == null || req.getPassword() == null) {
            result.put("error", "Email and password are required.");
            return result;
        }

        // Look up user by email
        User user = userRepository.findByEmail(req.getEmail());
        if (user == null) {
            result.put("error", "No account found with this email.");
            return result;
        }

        // Compare entered password with stored hash
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            result.put("error", "Incorrect password.");
            return result;
        }

        // Login success — return user info (no password hash!)
        result.put("id",    user.getId());
        result.put("name",  user.getName());
        result.put("email", user.getEmail());
        result.put("role",  user.getRole());
        return result;
    }
}
