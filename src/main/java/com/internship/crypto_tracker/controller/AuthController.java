package com.internship.crypto_tracker.controller;

import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.service.UserService;
import com.internship.crypto_tracker.util.JwtUtils; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils; 

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(
                user.getName(), 
                user.getEmail(), 
                user.getPassword()
            );
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<User> userBox = userService.findByEmail(email);

        if (userBox.isPresent()) {
            User user = userBox.get();
            
            if (passwordEncoder.matches(password, user.getPassword())) {
                
                String token = jwtUtils.generateJwtToken(email);
                
                
                return ResponseEntity.ok(Map.of("token", token));
            }
        }

        return ResponseEntity.status(401).body("Invalid email or password");
    }
}