package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequest;
import com.park.parkpro.dto.UserResponse;
import com.park.parkpro.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        UserResponse response = new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }
}