package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.*;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.security.JwtUtil;
import com.park.parkpro.service.UserService;
import com.park.parkpro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, AuthService authService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        User user = userService.createUser(request);
        UserResponseDto response = mapToUserResponseDto(user);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        User user = userService.signup(request);
        UserResponseDto response = mapToUserResponseDto(user);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        userService.verifyAccount(email, code);
        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/new-verification-code")
    public ResponseEntity<String> newVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.sendNewVerificationCode(email);
        return ResponseEntity.ok("New verification code sent");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.requestPasswordReset(email);
        return ResponseEntity.ok("Password reset link sent to your email");
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestParam String token, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/users/{userId}/parks/{parkId}")
    public ResponseEntity<Void> assignParkToUser(@PathVariable UUID userId, @PathVariable UUID parkId) {
        userService.assignParkToUser(userId, parkId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID parkId) {
        List<UserResponseDto> users;
        if (role != null && parkId != null) {
            throw new BadRequestException("Cannot filter by both role and parkId simultaneously");
        } else if (role != null) {
            users = userService.getUsersByRole(role).stream().map(this::mapToUserResponseDto).collect(Collectors.toList());
        } else if (parkId != null) {
            users = userService.getUsersByParkId(parkId).stream().map(this::mapToUserResponseDto).collect(Collectors.toList());
        } else {
            users = userService.getAllUsers().stream().map(this::mapToUserResponseDto).collect(Collectors.toList());
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(mapToUserResponseDto(user));
    }

    @GetMapping("/users/me")
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
        return ResponseEntity.ok(mapToUserResponseDto(user));
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        User updatedUser = userService.updateUserProfile(userId, request, token);
        return ResponseEntity.ok(mapToUserResponseDto(updatedUser));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        userService.deleteUser(userId, token);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/admin")
    public ResponseEntity<UserResponseDto> adminUpdateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        User updatedUser = userService.adminUpdateUser(userId, request, token);
        return ResponseEntity.ok(mapToUserResponseDto(updatedUser));
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                user.getGender(), user.getPassportNationalId(), user.getNationality(), user.getAge(),
                user.getRole(), user.getPark() != null ? user.getPark().getId() : null,
                user.isActive(), user.getLastLogin(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}