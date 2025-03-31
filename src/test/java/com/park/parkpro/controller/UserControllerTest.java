package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.dto.CreateUserRequest;
import com.park.parkpro.dto.LoginRequest;
import com.park.parkpro.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Login with the seeded admin user to get JWT token
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@example.com");
        login.setPassword("adminPass123");
        var loginResponse = restTemplate.postForEntity("/login", login, String.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        adminToken = loginResponse.getBody();
    }

    private HttpEntity<CreateUserRequest> createRequest(CreateUserRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @Test
    void createUserReturns201() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("landry@example.com");
        request.setPassword("password123");
        request.setRole("PARK_MANAGER"); // Using a different role for variety

        // Act
        var response = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request), UserResponse.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponse user = response.getBody();
        assertNotNull(user);
        assertEquals("landry@example.com", user.getEmail());
        assertEquals("PARK_MANAGER", user.getRole());
        assertNotNull(user.getId());
    }

    @Test
    void createUserWithDuplicateEmailReturns409() {
        // Arrange: Create first user
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        var firstResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponse.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Attempt duplicate
        CreateUserRequest request2 = new CreateUserRequest();
        request2.setFirstName("Jane");
        request2.setLastName("Doe");
        request2.setEmail("jean@example.com");
        request2.setPassword("newpass");
        request2.setRole("FINANCE_OFFICER");

        // Act
        var response = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request2), String.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Email 'jean@example.com' is already taken"));
    }
}