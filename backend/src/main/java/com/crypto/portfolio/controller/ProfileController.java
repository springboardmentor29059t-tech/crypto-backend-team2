package com.crypto.portfolio.controller;

import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

        @Autowired
        private UserRepository userRepository;

        @GetMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> getProfile(Authentication auth) {
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return ResponseEntity.ok(
                                Map.of(
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "profileImage",
                                                user.getProfileImage() != null ? user.getProfileImage() : "",
                                                "portfolioGoal",
                                                user.getPortfolioGoal() != null ? user.getPortfolioGoal() : "Long-term",
                                                "experienceLevel",
                                                user.getExperienceLevel() != null ? user.getExperienceLevel()
                                                                : "Intermediate",
                                                "riskPreference",
                                                user.getRiskPreference() != null ? user.getRiskPreference() : "Medium",
                                                // Handling comma separated list or sending raw string
                                                "preferredExchanges",
                                                user.getPreferredExchanges() != null
                                                                ? java.util.Arrays.asList(
                                                                                user.getPreferredExchanges().split(","))
                                                                : java.util.Collections.emptyList()));
        }

        @PutMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates, Authentication auth) {
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (updates.containsKey("fullName")) {
                        user.setFullName((String) updates.get("fullName"));
                }
                if (updates.containsKey("portfolioGoal")) {
                        user.setPortfolioGoal((String) updates.get("portfolioGoal"));
                }
                if (updates.containsKey("experienceLevel")) {
                        user.setExperienceLevel((String) updates.get("experienceLevel"));
                }
                if (updates.containsKey("riskPreference")) {
                        user.setRiskPreference((String) updates.get("riskPreference"));
                }
                if (updates.containsKey("preferredExchanges")) {
                        Object exchanges = updates.get("preferredExchanges");
                        if (exchanges instanceof java.util.List) {
                                String joined = String.join(",", (java.util.List<String>) exchanges);
                                user.setPreferredExchanges(joined);
                        }
                }

                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        }

        @PostMapping("/upload-image")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> uploadProfileImage(
                        @RequestParam("file") MultipartFile file,
                        Authentication authentication) throws IOException {

                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String uploadDir = "uploads/profile-pictures/";
                Files.createDirectories(Paths.get(uploadDir));

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + filename);
                Files.write(path, file.getBytes());

                user.setProfileImage("/uploads/profile-pictures/" + filename);
                userRepository.save(user);

                return ResponseEntity.ok(
                                Map.of("imageUrl", user.getProfileImage()));
        }
}