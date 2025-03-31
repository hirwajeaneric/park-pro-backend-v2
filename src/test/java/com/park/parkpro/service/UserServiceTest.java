package com.park.parkpro.service;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        when(userRepository.findByEmail("jean@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        User savedUser = new User();
        savedUser.setFirstName("Jean");
        savedUser.setLastName("Dupont");
        savedUser.setEmail("jean@example.com");
        savedUser.setPassword("hashedPassword");
        savedUser.setRole("ADMIN");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("jean@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        // Arrange
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        when(userRepository.findByEmail("jean@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });
        assertEquals("Email 'jean@example.com' is already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionForInvalidRole() {
        // Arrange
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("INVALID_ROLE");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        });
        assertEquals("Invalid role: INVALID_ROLE", exception.getMessage());
    }

    @Test
    void shouldSignUpVisitorSuccessfully() {
        // Arrange
        SignupRequestDto request = new SignupRequestDto();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("visitorPass123")).thenReturn("hashedVisitorPass");
        User savedUser = new User();
        savedUser.setFirstName("Alice");
        savedUser.setLastName("Smith");
        savedUser.setEmail("alice@example.com");
        savedUser.setPassword("hashedVisitorPass");
        savedUser.setRole("VISITOR");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.signup(request);

        // Assert
        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("VISITOR", result.getRole());
        assertEquals("hashedVisitorPass", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionForDuplicateEmailDuringSignup() {
        // Arrange
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signup(request);
        });
        assertEquals("Email 'alice@example.com' is already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}