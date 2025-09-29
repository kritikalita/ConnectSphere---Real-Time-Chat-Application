package com.chat.app.controller;

import com.chat.app.model.User;
import com.chat.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    // --- MODIFIED METHOD ---
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        // 1. Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            // 2. If it exists, redirect back with an error
            return "redirect:/register?error";
        }

        // 3. If it doesn't exist, proceed with saving the new user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login?success";
    }
}