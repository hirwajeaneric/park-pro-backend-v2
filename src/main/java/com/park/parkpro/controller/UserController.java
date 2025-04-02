package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.security.JwtUtil;
import com.park.parkpro.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserRequestDto request) {
        User user = userService.createUser(request);
        UserResponseDto response = new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }

    @PostMapping("/{userId}/parks/{parkId}")
    public ResponseEntity<Void> assignParkToUser(@PathVariable UUID userId, @PathVariable UUID parkId) {
        userService.assignParkToUser(userId, parkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID parkId) {
        List<UserResponseDto> users;
        if (role != null && parkId != null) {
            throw new IllegalArgumentException("Cannot filter by both role and parkId simultaneously");
        } else if (role != null) {
            users = userService.getUsersByRole(role).stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole()))
                    .collect(Collectors.toList());
        } else if (parkId != null) {
            users = userService.getUsersByParkId(parkId).stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole()))
                    .collect(Collectors.toList());
        } else {
            users = userService.getAllUsers().stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Entering getCurrentUser with authHeader: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Invalid or missing Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String token = authHeader.substring(7);
        System.out.println("Extracted token: " + token);
        if (!jwtUtil.validateToken(token)) {
            System.out.println("Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String email = jwtUtil.getEmailFromToken(token);
        System.out.println("Token validated, email: " + email);
        User user = userService.getUserByEmail(email);
        System.out.println("User retrieved: " + user.getFirstName() + " " + user.getLastName());
        UUID parkId = (user.getPark() != null) ? user.getPark().getId() : null;
        System.out.println("Park id: " + parkId);
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), parkId
        );
        System.out.println("Returning response for: " + email);
        return ResponseEntity.ok(response);
    }
}