package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

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
}