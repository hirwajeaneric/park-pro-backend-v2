package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.domain.Park;
import com.park.parkpro.dto.CreateParkRequestDto;
import com.park.parkpro.dto.CreateUserRequestDto;
import com.park.parkpro.dto.LoginRequestDto;
import com.park.parkpro.dto.UserResponseDto;
import com.park.parkpro.repository.UserRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clear the user table before each test
        userRepository.deleteAll();

        // Login with the seeded admin user to get JWT token (TestConfig re-seeds it)
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

    private HttpEntity<CreateParkRequestDto> createParkRequest(CreateParkRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @Test
    void createUserReturns201() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("PARK_MANAGER");

        var response = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request), UserResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponseDto user = response.getBody();
        assertNotNull(user);
        assertEquals("jean@example.com", user.getEmail());
        assertEquals("PARK_MANAGER", user.getRole());
        assertNotNull(user.getId());
    }

    @Test
    void createUserWithDuplicateEmailReturns409() {
        CreateUserRequestDto request1 = new CreateUserRequestDto();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        var firstResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponseDto.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        CreateUserRequestDto request2 = new CreateUserRequestDto();
        request2.setFirstName("Jane");
        request2.setLastName("Doe");
        request2.setEmail("jean@example.com");
        request2.setPassword("newpass");
        request2.setRole("FINANCE_OFFICER");

        var response = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request2), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Email 'jean@example.com' is already taken"));
    }

    @Test
    void assignParkToUserReturns200() {
        // Create a user
        CreateUserRequestDto userRequest = new CreateUserRequestDto();
        userRequest.setFirstName("Manager");
        userRequest.setLastName("One");
        userRequest.setEmail("manager@example.com");
        userRequest.setPassword("pass123");
        userRequest.setRole("PARK_MANAGER");
        var userResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(userRequest), UserResponseDto.class);
        UUID userId = userResponse.getBody().getId();

        // Create a park
        CreateParkRequestDto parkRequest = new CreateParkRequestDto("Loango", "Southwest Gabon", "Coastal park");
        var parkResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createParkRequest(parkRequest), Park.class);
        UUID parkId = parkResponse.getBody().getId();

        // Assign park to user
        var assignResponse = restTemplate.exchange("/api/users/" + userId + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        assertEquals(HttpStatus.OK, assignResponse.getStatusCode());

        // Verify assignment (could add a GET endpoint later to check)
    }
}