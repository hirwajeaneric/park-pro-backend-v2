package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.LoginRequest;
import com.park.parkpro.dto.SignupRequest;
import com.park.parkpro.dto.UserResponse;
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
        SignupRequest signup = new SignupRequest();
        signup.setFirstName("Alice");
        signup.setLastName("Smith");
        signup.setEmail("alice@example.com");
        signup.setPassword("visitorPass123");
        restTemplate.postForEntity("/api/signup", signup, UserResponse.class);

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request);

        // Act
        var response = restTemplate.postForEntity("/login", entity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody()); // Token
    }

    @Test
    void signupReturns201() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPassword("visitorPass123");
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request);

        // Act
        var response = restTemplate.postForEntity("/api/signup", entity, UserResponse.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponse user = response.getBody();
        assertNotNull(user);
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("VISITOR", user.getRole());
        assertNotNull(user.getId());
    }

    @Test
    void signupWithDuplicateEmailReturns409() {
        // Arrange: Create first user
        SignupRequest request1 = new SignupRequest();
        request1.setFirstName("Alice");
        request1.setLastName("Smith");
        request1.setEmail("alice@example.com");
        request1.setPassword("visitorPass123");
        var firstResponse = restTemplate.postForEntity("/api/signup", request1, UserResponse.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Attempt duplicate
        SignupRequest request2 = new SignupRequest();
        request2.setFirstName("Alice");
        request2.setLastName("James");
        request2.setEmail("alice@example.com");
        request2.setPassword("newPass");
        HttpEntity<SignupRequest> entity = new HttpEntity<>(request2);

        // Act
        var response = restTemplate.postForEntity("/api/signup", entity, String.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Email 'alice@example.com' is already taken"),
                "Expected error message not found in response: " + response.getBody());
    }
}