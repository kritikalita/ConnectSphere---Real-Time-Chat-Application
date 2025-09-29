package com.chat.app.controller;

import com.chat.app.model.User;
import com.chat.app.repository.UserRepository;
import com.chat.app.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Endpoint to get the current user's profile
    @GetMapping
    public ResponseEntity<User> getCurrentUserProfile(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .map(user -> {
                    user.setPassword(null); // Never send the password hash
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to upload a new avatar
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        user.setAvatarUrl(fileDownloadUri);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("avatarUrl", fileDownloadUri));
    }

    // Endpoint to update the status message
    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, String> payload, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String status = payload.get("status");
        user.setStatusMessage(status);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("status", status));
    }
}