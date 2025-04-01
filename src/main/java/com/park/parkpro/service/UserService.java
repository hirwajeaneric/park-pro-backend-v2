package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.exception.IllegalArgumentException;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "FINANCE_OFFICER", "PARK_MANAGER", "VISITOR", "GOVERNMENT_OFFICER", "AUDITOR");
    private final ParkRepository parkRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ParkRepository parkRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.parkRepository = parkRepository;
    }

    public User createUser(CreateUserRequestDto request) {
        // Validate inputs
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (!VALID_ROLES.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already taken");
        }

        // Create and save user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    public User signup(SignupRequestDto request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already taken");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VISITOR"); // Fixed role for signup
        return userRepository.save(user);
    }

    public void assignParkToUser(UUID userId, UUID parkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID '" + userId + "' not found"));
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("Park with ID '" + parkId + "' not found"));
        user.setPark(park);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersByParkId(UUID parkId) {
        return userRepository.findByParkId(parkId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email '" + email + "' not found"));
    }
}