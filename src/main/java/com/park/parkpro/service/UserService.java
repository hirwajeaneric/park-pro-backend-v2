package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ConflictException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParkRepository parkRepository;
    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "FINANCE_OFFICER", "PARK_MANAGER", "VISITOR", "GOVERNMENT_OFFICER", "AUDITOR");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ParkRepository parkRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.parkRepository = parkRepository;
    }

    @Transactional
    public User createUser(CreateUserRequestDto request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        if (!VALID_ROLES.contains(request.getRole())) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already taken");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    @Transactional
    public User signup(SignupRequestDto request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already taken");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VISITOR"); // Fixed role for signup
        return userRepository.save(user);
    }

    @Transactional
    public void assignParkToUser(UUID userId, UUID parkId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID '" + userId + "' not found"));
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park with ID '" + parkId + "' not found"));
        if (!"PARK_MANAGER".equals(user.getRole())) {
            throw new BadRequestException("Only PARK_MANAGER users can be assigned to a park");
        }
        if (user.getPark() != null) {
            throw new ConflictException("User " + userId + " is already assigned to park " + user.getPark().getId());
        }

        user.setPark(park);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new BadRequestException("Invalid role: " + role);
        }
        List<User> users = userRepository.findByRole(role);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found with role: " + role);
        }
        return users;
    }

    public List<User> getUsersByParkId(UUID parkId) {
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park with ID '" + parkId + "' not found");
        }
        List<User> users = userRepository.findByParkId(parkId);
        if (users.isEmpty()) {
            throw new NotFoundException("No users found for park with ID: " + parkId);
        }
        return users;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email '" + email + "' not found"));
    }
}