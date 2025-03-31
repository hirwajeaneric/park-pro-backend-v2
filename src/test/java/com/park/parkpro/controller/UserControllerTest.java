package com.park.parkpro.controller;

import com.park.parkpro.dto.CreateUserRequest;
import com.park.parkpro.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUserReturns201() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request);

        // Act
        var response = restTemplate.exchange("/api/users", HttpMethod.POST, entity, UserResponse.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponse user = response.getBody();
        assertNotNull(user);
        assertEquals("jean@example.com", user.getEmail());
        assertEquals("ADMIN", user.getRole());
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
        request1.setRole("ADMIN");
        restTemplate.postForEntity("/api/users", request1, UserResponse.class);

        // Attempt duplicate
        CreateUserRequest request2 = new CreateUserRequest();
        request2.setEmail("jean@example.com");
        request2.setPassword("newpass");
        request2.setRole("PARK_MANAGER");
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request2);

        // Act
        var response = restTemplate.exchange("/api/users", HttpMethod.POST, entity, String.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Email 'jean@example.com' is already taken"));
    }
}