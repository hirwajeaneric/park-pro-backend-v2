package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.security.JwtUtil;
import com.park.parkpro.service.UserService;
import jakarta.validation.Valid;
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
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        User user = userService.createUser(request);
        UserResponseDto response = new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), null);
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
            throw new BadRequestException("Cannot filter by both role and parkId simultaneously");
        } else if (role != null) {
            users = userService.getUsersByRole(role).stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), user.getPark() != null ? user.getPark().getId() : null))
                    .collect(Collectors.toList());
        } else if (parkId != null) {
            users = userService.getUsersByParkId(parkId).stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), user.getPark() != null ? user.getPark().getId() : null))
                    .collect(Collectors.toList());
        } else {
            users = userService.getAllUsers().stream()
                    .map(user -> new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), user.getPark() != null ? user.getPark().getId() : null))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired JWT token");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userService.getUserByEmail(email);
        UUID parkId = (user.getPark() != null) ? user.getPark().getId() : null;
        UserResponseDto response = new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), parkId
        );
        return ResponseEntity.ok(response);
    }
}