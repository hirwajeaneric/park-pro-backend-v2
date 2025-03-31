package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.LoginRequestDto;
import com.park.parkpro.dto.UserResponseDto;
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
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("admin@example.com");
        login.setPassword("adminPass123");
        var loginResponse = restTemplate.postForEntity("/login", login, String.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        adminToken = loginResponse.getBody();
    }

    private HttpEntity<CreateUserRequestDto> createRequest(CreateUserRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @Test
    void createUserReturns201() {
        // Arrange
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("landry@example.com");
        request.setPassword("password123");
        request.setRole("PARK_MANAGER"); // Using a different role for variety

        // Act
        var response = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request), UserResponseDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponseDto user = response.getBody();
        assertNotNull(user);
        assertEquals("landry@example.com", user.getEmail());
        assertEquals("PARK_MANAGER", user.getRole());
        assertNotNull(user.getId());
    }

    @Test
    void createUserWithDuplicateEmailReturns409() {
        // Arrange: Create first user
        CreateUserRequestDto request1 = new CreateUserRequestDto();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        var firstResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponseDto.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Attempt duplicate
        CreateUserRequestDto request2 = new CreateUserRequestDto();
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