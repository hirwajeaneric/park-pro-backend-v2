package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
}