package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.domain.Park;
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

import java.util.List;
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
        userRepository.deleteAll();
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("admin@example.com");
        login.setPassword("adminPass123");
        var loginResponse = restTemplate.postForEntity("/login", login, String.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        adminToken = loginResponse.getBody();
    }

    private HttpEntity<?> createRequest(Object request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<?> createGetRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<?> createGetRequestWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
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
        CreateUserRequestDto userRequest = new CreateUserRequestDto();
        userRequest.setFirstName("Manager");
        userRequest.setLastName("One");
        userRequest.setEmail("manager@example.com");
        userRequest.setPassword("pass123");
        userRequest.setRole("PARK_MANAGER");
        var userResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(userRequest), UserResponseDto.class);
        UUID userId = userResponse.getBody().getId();

        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var parkResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = parkResponse.getBody().getId();

        var assignResponse = restTemplate.exchange("/api/users/" + userId + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        assertEquals(HttpStatus.OK, assignResponse.getStatusCode());
    }

    @Test
    void getAllUsersReturns200() {
        CreateUserRequestDto request1 = new CreateUserRequestDto();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponseDto.class);

        CreateUserRequestDto request2 = new CreateUserRequestDto();
        request2.setFirstName("Jane");
        request2.setLastName("Doe");
        request2.setEmail("jane@example.com");
        request2.setPassword("pass456");
        request2.setRole("FINANCE_OFFICER");
        restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request2), UserResponseDto.class);

        var response = restTemplate.exchange("/api/users", HttpMethod.GET, createGetRequest(), UserResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDto[] users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.length);
        assertTrue(List.of(users).stream().anyMatch(u -> u.getEmail().equals("jean@example.com")));
        assertTrue(List.of(users).stream().anyMatch(u -> u.getEmail().equals("jane@example.com")));
    }

    @Test
    void getUsersByRoleReturns200() {
        CreateUserRequestDto request1 = new CreateUserRequestDto();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponseDto.class);

        CreateUserRequestDto request2 = new CreateUserRequestDto();
        request2.setFirstName("Jane");
        request2.setLastName("Doe");
        request2.setEmail("jane@example.com");
        request2.setPassword("pass456");
        request2.setRole("FINANCE_OFFICER");
        restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request2), UserResponseDto.class);

        var response = restTemplate.exchange("/api/users?role=PARK_MANAGER", HttpMethod.GET, createGetRequest(), UserResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDto[] users = response.getBody();
        assertNotNull(users);
        assertEquals(1, users.length);
        assertEquals("jean@example.com", users[0].getEmail());
        assertEquals("PARK_MANAGER", users[0].getRole());
    }

    @Test
    void getUsersByParkIdReturns200() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var parkResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = parkResponse.getBody().getId();

        CreateUserRequestDto request1 = new CreateUserRequestDto();
        request1.setFirstName("Jean");
        request1.setLastName("Dupont");
        request1.setEmail("jean@example.com");
        request1.setPassword("password123");
        request1.setRole("PARK_MANAGER");
        var userResponse1 = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request1), UserResponseDto.class);
        UUID userId1 = userResponse1.getBody().getId();
        restTemplate.exchange("/api/users/" + userId1 + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        CreateUserRequestDto request2 = new CreateUserRequestDto();
        request2.setFirstName("Jane");
        request2.setLastName("Doe");
        request2.setEmail("jane@example.com");
        request2.setPassword("pass456");
        request2.setRole("PARK_MANAGER");
        var userResponse2 = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request2), UserResponseDto.class);
        UUID userId2 = userResponse2.getBody().getId();
        restTemplate.exchange("/api/users/" + userId2 + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        var response = restTemplate.exchange("/api/users?parkId=" + parkId, HttpMethod.GET, createGetRequest(), UserResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDto[] users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.length);
        assertTrue(List.of(users).stream().anyMatch(u -> u.getEmail().equals("jean@example.com")));
        assertTrue(List.of(users).stream().anyMatch(u -> u.getEmail().equals("jane@example.com")));
    }

    @Test
    void getUsersWithNoAuthReturns401() {
        var response = restTemplate.exchange("/api/users", HttpMethod.GET, new HttpEntity<>(null), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUserReturns200() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("password123");
        request.setRole("PARK_MANAGER");
        var createResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(request), UserResponseDto.class);
        UUID userId = createResponse.getBody().getId();

        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var parkResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = parkResponse.getBody().getId();

        restTemplate.exchange("/api/users/" + userId + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("jean@example.com");
        loginRequest.setPassword("password123");
        var loginResponse = restTemplate.postForEntity("/login", loginRequest, String.class);
        String userToken = loginResponse.getBody();

        var response = restTemplate.exchange("/api/users/me", HttpMethod.GET, createGetRequestWithToken(userToken), UserResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDto user = response.getBody();
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals("jean@example.com", user.getEmail());
        assertEquals("Jean", user.getFirstName());
        assertEquals("Dupont", user.getLastName());
        assertEquals("PARK_MANAGER", user.getRole());
        assertEquals(parkId, user.getParkId());
    }
}