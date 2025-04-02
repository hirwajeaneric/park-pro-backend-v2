package com.park.parkpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DebugController {
    @GetMapping("/debug/auth")
    public ResponseEntity<String> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Auth: " + (auth != null ? auth.toString() : "null"));
    }
}