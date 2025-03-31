package com.park.parkpro.controller;

import com.park.parkpro.domain.Park;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ParkControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpEntity<Park> createRequest(Park park) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("testuser", "testpass");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(park, headers);
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
        assertNull(createdPark.getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains(createdPark.getId().toString()));
    }

    @Test
    void postParkWithDuplicateNameReturns400() {
        // Arrange: Create first park
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST ,createRequest(park1), String.class);

        // Attempt duplicate
        Park park2 = new Park("Loango", "Southwest Gabon", "Coastal park");

        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), String.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(("Park with name 'Loango' already exists")));
    }

    @Test
    void postParkWithoutAuthReturns401() {
        // Arrange
        Park park = new Park("Ivindo", "Southeast Gabon", "Rain forest and park");
        HttpEntity<Park> request = new HttpEntity<>(park);

        // Act
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, request, String.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
