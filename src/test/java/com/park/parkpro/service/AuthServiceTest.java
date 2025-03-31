package com.park.parkpro.service;

import com.park.parkpro.domain.User;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("encodedPass");
        user.setRole("ADMIN");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("admin@example.com", "ADMIN")).thenReturn("jwt-token");

        String token = authService.login("admin@example.com", "password");
        assertEquals("jwt-token", token);
    }

    @Test
    void shouldThrowExceptionForUnknownUser() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login("unknown@example.com", "password"));
    }

    @Test
    void shouldThrowExceptionForInvalidPassword() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("encodedPass");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.login("admin@example.com", "wrong"));
    }
}