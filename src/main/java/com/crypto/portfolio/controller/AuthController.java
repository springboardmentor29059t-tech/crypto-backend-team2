package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.AuthResponse;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.JwtService;
import com.crypto.portfolio.service.UserActivityLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserActivityLogService activityLogService;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody com.crypto.portfolio.dto.RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with this email");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                encodedPassword);

        User savedUser = userRepository.save(user);
        activityLogService.logActivity(savedUser, "REGISTER", "User registered successfully");

        com.crypto.portfolio.dto.UserDto userDto = new com.crypto.portfolio.dto.UserDto(savedUser.getId(),
                savedUser.getFullName(), savedUser.getEmail(), savedUser.getProfileImage());

        String jwtToken = jwtService.generateToken(savedUser);
        return new AuthResponse(jwtToken, userDto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody com.crypto.portfolio.dto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        activityLogService.logActivity(user, "LOGIN", "User logged in successfully");

        com.crypto.portfolio.dto.UserDto userDto = new com.crypto.portfolio.dto.UserDto(user.getId(),
                user.getFullName(), user.getEmail(), user.getProfileImage());

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, userDto);
    }
}