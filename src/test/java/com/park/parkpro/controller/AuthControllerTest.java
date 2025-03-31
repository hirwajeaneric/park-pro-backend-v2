package com.park.parkpro.controller;

import com.park.parkpro.dto.LoginRequestDto;
import com.park.parkpro.dto.SignupRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clear test DB
    }

    @Test
    void loginReturnsToken() {
        // Arrange: Create a user first
        SignupRequestDto signup = new SignupRequestDto();
        signup.setFirstName("Alice");
        signup.setLastName("Smith");
        signup.setEmail("alice@example.com");
        signup.setPassword("visitorPass123");
        restTemplate.postForEntity("/api/signup", signup, UserResponseDto.class);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");
        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(request);

        // Act
        var response = restTemplate.postForEntity("/login", entity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody()); // Token
    }

    @Test
    void signupReturns201() {
        // Arrange
        SignupRequestDto request = new SignupRequestDto();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");
        HttpEntity<SignupRequestDto> entity = new HttpEntity<>(request);

        // Act
        var response = restTemplate.postForEntity("/api/signup", entity, UserResponseDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponseDto user = response.getBody();
        assertNotNull(user);
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("VISITOR", user.getRole());
        assertNotNull(user.getId());
    }

    @Test
    void signupWithDuplicateEmailReturns409() {
        // Arrange: Create first user
        SignupRequestDto request1 = new SignupRequestDto();
        request1.setFirstName("Alice");
        request1.setLastName("Smith");
        request1.setEmail("alice@example.com");
        request1.setPassword("visitorPass123");
        var firstResponse = restTemplate.postForEntity("/api/signup", request1, UserResponseDto.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Attempt duplicate
        SignupRequestDto request2 = new SignupRequestDto();
        request2.setFirstName("Alice");
        request2.setLastName("James");
        request2.setEmail("alice@example.com");
        request2.setPassword("newPass");
        HttpEntity<SignupRequestDto> entity = new HttpEntity<>(request2);

        // Act
        var response = restTemplate.postForEntity("/api/signup", entity, String.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Email 'alice@example.com' is already taken"),
                "Expected error message not found in response: " + response.getBody());
    }
}