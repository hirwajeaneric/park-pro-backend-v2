package com.park.parkpro.service;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistUserWithHashedPassword() {
        // Arrange
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        // Act
        User user = userService.createUser(request);

        // Assert
        assertNotNull(user.getId());
        assertEquals("jean@example.com", user.getEmail());
        assertNotEquals("password123", user.getPassword()); // Should be hashed
        assertTrue(userRepository.findByEmail("jean@example.com").isPresent());
    }
}