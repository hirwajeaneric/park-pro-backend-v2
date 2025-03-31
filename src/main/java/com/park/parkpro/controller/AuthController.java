package com.park.parkpro.controller;

import com.park.parkpro.dto.LoginRequest;
import com.park.parkpro.dto.SignupRequest;
import com.park.parkpro.dto.UserResponse;
import com.park.parkpro.service.AuthService;
import com.park.parkpro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/api/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        var user = userService.signup(request);
        UserResponse response = new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }
}