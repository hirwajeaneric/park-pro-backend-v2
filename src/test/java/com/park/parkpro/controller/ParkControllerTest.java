package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.domain.Park;
import com.park.parkpro.dto.*;
import com.park.parkpro.repository.ParkRepository;
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
class ParkControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ParkRepository parkRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clear the park table before each test
        parkRepository.deleteAll();

        // Login with the seeded admin user to get JWT token
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("admin@example.com");
        login.setPassword("adminPass123");
        var loginResponse = restTemplate.postForEntity("/login", login, String.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        adminToken = loginResponse.getBody();
    }

    private HttpEntity<?> createRequest(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> createGetRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        return new HttpEntity<>(headers);
    }

    @Test
    void postParkReturns201WithParkDetails() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);

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
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);

        Park park2 = new Park("Loango", "New Location", "Different desc");
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with name 'Loango' already exists"));
    }

    @Test
    void postParkWithoutAuthReturns401() {
        Park park = new Park("Ivindo", "Southeast Gabon", "Rain forest and park");
        HttpEntity<Park> request = new HttpEntity<>(park);
        var response = restTemplate.exchange("/api/parks", HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getParksReturns200WithParkList() {
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        Park park2 = new Park("Ivindo", "Southeast Gabon", "Rainforest park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), Park.class);

        var response = restTemplate.exchange("/api/parks", HttpMethod.GET, createGetRequest(), Park[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park[] parks = response.getBody();
        assertNotNull(parks);
        assertEquals(2, parks.length);
        assertEquals("Loango", parks[0].getName());
        assertEquals("Ivindo", parks[1].getName());
    }

    @Test
    void getParksWithoutAuthReturns401() {
        var response = restTemplate.exchange("/api/parks", HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getParkByIdReturns200() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = createResponse.getBody().getId();

        var response = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.GET, createGetRequest(), Park.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park retrievedPark = response.getBody();
        assertNotNull(retrievedPark);
        assertEquals("Loango", retrievedPark.getName());
        assertEquals("Southwest Gabon", retrievedPark.getLocation());
        assertEquals("Coastal park", retrievedPark.getDescription());
    }

    @Test
    void getParkByIdNotFoundReturns404() {
        UUID randomId = UUID.randomUUID();
        var response = restTemplate.exchange("/api/parks/" + randomId, HttpMethod.GET, createGetRequest(), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with ID '" + randomId + "' not found"));
    }

    @Test
    void putParkUpdatesSuccessfullyReturns200() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = createResponse.getBody().getId();

        Park updatedPark = new Park("Loango Updated", "North Gabon", "Updated coastal park");
        var response = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.PUT, createRequest(updatedPark), Park.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park result = response.getBody();
        assertNotNull(result);
        assertEquals("Loango Updated", result.getName());
        assertEquals("North Gabon", result.getLocation());
        assertEquals("Updated coastal park", result.getDescription());
    }

    @Test
    void putParkWithDuplicateNameReturns409() {
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse1 = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        UUID parkId1 = createResponse1.getBody().getId();

        Park park2 = new Park("Ivindo", "Southeast Gabon", "Rainforest park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), Park.class);

        Park updatedPark = new Park("Ivindo", "North Gabon", "Updated park");
        var response = restTemplate.exchange("/api/parks/" + parkId1, HttpMethod.PUT, createRequest(updatedPark), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with name 'Ivindo' already exists"));
    }

    @Test
    void putParkNotFoundReturns404() {
        UUID randomId = UUID.randomUUID();
        Park updatedPark = new Park("Nonexistent", "Somewhere", "No park");
        var response = restTemplate.exchange("/api/parks/" + randomId, HttpMethod.PUT, createRequest(updatedPark), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with ID '" + randomId + "' not found"));
    }

    @Test
    void deleteParkReturns204() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = createResponse.getBody().getId();

        var response = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.DELETE, createGetRequest(), Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // Verify park is deleted
        var getResponse = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.GET, createGetRequest(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void deleteParkNotFoundReturns404() {
        UUID randomId = UUID.randomUUID();
        var response = restTemplate.exchange("/api/parks/" + randomId, HttpMethod.DELETE, createGetRequest(), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with ID '" + randomId + "' not found"));
    }

    @Test
    void getParksWithFilteringReturns200() {
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        Park park2 = new Park("Ivindo", "Southeast Gabon", "Rainforest park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), Park.class);
        Park park3 = new Park("Lope", "Central Gabon", "Savanna park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park3), Park.class);

        var response = restTemplate.exchange("/api/parks?name=lo", HttpMethod.GET, createGetRequest(), Park[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park[] parks = response.getBody();
        assertNotNull(parks);
        assertEquals(2, parks.length); // Loango and Lope
        assertTrue(List.of(parks).stream().anyMatch(p -> p.getName().equals("Loango")));
        assertTrue(List.of(parks).stream().anyMatch(p -> p.getName().equals("Lope")));
    }

    @Test
    void getParksWithFilterningReturns200() {
        for (int i = 1; i <= 15; i++) {
            Park park = new Park("Park" + i, "Location" + i, "Desc" + i);
            restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        }

        var response = restTemplate.exchange("/api/parks?page=1&size=5", HttpMethod.GET, createGetRequest(), Park[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park[] parks = response.getBody();
        assertNotNull(parks);
        assertEquals(5, parks.length); // Page 1, size 5 (parks 6-10)
        assertEquals("Park6", parks[0].getName());
        assertEquals("Park10", parks[4].getName());
    }

    @Test
    void getParksWithPaginationReturns200() {
        for (int i = 1; i <= 15; i++) {
            Park park = new Park("Park" + i, "Location" + i, "Desc" + i);
            restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        }

        var response = restTemplate.exchange("/api/parks?page=1&size=5", HttpMethod.GET, createGetRequest(), PageResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageResponseDto<Park> page = response.getBody();
        assertNotNull(page);
        assertEquals(5, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(3, page.getTotalPages()); // 15 items, 5 per page = 3 pages
        assertEquals(1, page.getCurrentPage());
        assertEquals(5, page.getPageSize());
        assertEquals("Park6", page.getContent().get(0).getName());
        assertEquals("Park10", page.getContent().get(4).getName());
    }

    @Test
    void patchParkUpdatesPartiallyReturns200() {
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = createResponse.getBody().getId();

        PatchParkRequestDto patchRequest = new PatchParkRequestDto();
        patchRequest.setLocation("North Gabon");
        var response = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.PATCH, createRequest(patchRequest), Park.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Park updatedPark = response.getBody();
        assertNotNull(updatedPark);
        assertEquals("Loango", updatedPark.getName()); // Unchanged
        assertEquals("North Gabon", updatedPark.getLocation());
        assertEquals("Coastal park", updatedPark.getDescription()); // Unchanged
    }

    @Test
    void patchParkWithDuplicateNameReturns409() {
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse1 = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park1), Park.class);
        UUID parkId1 = createResponse1.getBody().getId();

        Park park2 = new Park("Ivindo", "Southeast Gabon", "Rainforest park");
        restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park2), Park.class);

        PatchParkRequestDto patchRequest = new PatchParkRequestDto();
        patchRequest.setName("Ivindo");
        var response = restTemplate.exchange("/api/parks/" + parkId1, HttpMethod.PATCH, createRequest(patchRequest), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with name 'Ivindo' already exists"));
    }

    @Test
    void patchParkNotFoundReturns404() {
        UUID randomId = UUID.randomUUID();
        PatchParkRequestDto patchRequest = new PatchParkRequestDto();
        patchRequest.setName("Nonexistent");
        var response = restTemplate.exchange("/api/parks/" + randomId, HttpMethod.PATCH, createRequest(patchRequest), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Park with ID '" + randomId + "' not found"));
    }

    @Test
    void deleteParkReturns204AndCascades() {
        CreateUserRequestDto userRequest = new CreateUserRequestDto();
        userRequest.setFirstName("Manager");
        userRequest.setLastName("One");
        userRequest.setEmail("manager@example.com");
        userRequest.setPassword("pass123");
        userRequest.setRole("PARK_MANAGER");
        var userResponse = restTemplate.exchange("/api/users", HttpMethod.POST, createRequest(userRequest), UserResponseDto.class);
        UUID userId = userResponse.getBody().getId();

        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        var createResponse = restTemplate.exchange("/api/parks", HttpMethod.POST, createRequest(park), Park.class);
        UUID parkId = createResponse.getBody().getId();

        restTemplate.exchange("/api/users/" + userId + "/parks/" + parkId, HttpMethod.POST, createRequest(null), Void.class);

        var deleteResponse = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.DELETE, createGetRequest(), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        var getParkResponse = restTemplate.exchange("/api/parks/" + parkId, HttpMethod.GET, createGetRequest(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getParkResponse.getStatusCode());
    }
}