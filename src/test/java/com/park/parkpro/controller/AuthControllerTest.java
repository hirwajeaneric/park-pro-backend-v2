package com.park.parkpro.controller;

import com.park.parkpro.domain.User;
import com.park.parkpro.dto.LoginRequest;
import com.park.parkpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("$2a$10$examplehash"); // BCrypt hash for "password"
        user.setRole("ADMIN");
        userRepository.save(user);
    }

    @Test
    void loginReturnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password");
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request);

        var response = restTemplate.postForEntity("/login", entity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody()); // Token
    }
}