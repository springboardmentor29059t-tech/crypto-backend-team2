package com.crypto.tracker.controller;

import com.crypto.tracker.model.ScamToken;
import com.crypto.tracker.repository.ScamTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "http://localhost:5173")
public class ScamTokenController {

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    @GetMapping("/scams")
    public List<ScamToken> getAllScams() {
        return scamTokenRepository.findAll();
    }
}