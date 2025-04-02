package com.park.parkpro.controller;

import com.park.parkpro.dto.LoginRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.dto.UserResponseDto;
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
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/api/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody SignupRequestDto request) {
        var user = userService.signup(request);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), null
        );
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }
}