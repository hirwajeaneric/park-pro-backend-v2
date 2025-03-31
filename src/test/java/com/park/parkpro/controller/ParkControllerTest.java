package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.domain.Park;
import com.park.parkpro.dto.LoginRequestDto;
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
class ParkControllerTest {

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

    private HttpEntity<?> createRequest(Park park) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(park, headers);
    }

    private HttpEntity<?> createGetRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        return new HttpEntity<>(headers);
    }

    @Test
    void postParkReturns201WithParkDetails() {
        // Arrange
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");

        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Park createdPark = response.getBody();
        assertNotNull(createdPark);
        assertEquals("Loango", createdPark.getName());
        assertEquals("Southwest Gabon", createdPark.getLocation());
        assertEquals("Coastal park", createdPark.getDescription());
        assertTrue(response.getHeaders().getLocation().toString().contains(createdPark.getId().toString()));
    }

    @Test
    void postParkWithDuplicateNameReturns409() {
        // Arrange: Create first park
        Park park1 = new Park("Ivindo", "Southwest Gabon", "Coastal park");
        var firstResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());
        // Attempt duplicate
        Park park2 = new Park("Ivindo", "New Location", "Different desc");
        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), String.class);
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with name 'Ivindo' already exists"));
    }

    @Test
    void postParkWithoutAuthReturns401() {
        // Arrange
        Park park = new Park("Lelo", "Southeast Gabon", "Rain forest and park");
        HttpEntity<Park> request = new HttpEntity<>(park);
        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, request, String.class);
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getParksReturns200WithParkList() {
        // Arrange: Create some parks
        Park park1 = new Park("Abc Park", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        Park park2 = new Park("Xyz Park", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), Park.class);
        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.GET, createGetRequest(), Park[].class);
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park[] parks = response.getBody();
        assertNotNull(parks);
        assertEquals(2, parks.length);
        assertEquals("Abc Park", parks[0].getName());
        assertEquals("Xyz Park", parks[1].getName());
    }

    @Test
    void getParkWithoutAuthReturns401() {
        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.GET, new HttpEntity<>(null), String.class);
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}