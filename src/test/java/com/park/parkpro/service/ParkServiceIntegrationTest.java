package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = "ADMIN") // Mock an authorized user
class ParkServiceIntegrationTest {

    @Autowired
    private ParkService parkService;

    @Autowired
    private ParkRepository parkRepository;

    @Test
    void shouldPersistParkInDatabase() {
        // Arrange
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        // Act
        Park savedPark = parkService.createPark(park);

        // Assert
        assertNotNull(savedPark.getId());
        assertNotNull(savedPark.getCreatedAt());
        assertNotNull(savedPark.getUpdatedAt());
        assertTrue(parkRepository.findById(savedPark.getId()).isPresent());
    }

    @Test
    void shouldFailOnDuplicateName() {
        // Arrange
        Park park1 = new Park("Lopé National Park", "Central Gabon", "UNESCO site with ancient forests");
        parkService.createPark(park1);

        Park park2 = new Park("Lopé National Park", "Central Gabon", "UNESCO site with ancient forests");

        // Act & Assert
        DuplicateParkException exception = assertThrows(DuplicateParkException.class, () -> parkService.createPark(park2));
        assertEquals("Park with name 'Lopé National Park' already exists", exception.getMessage());
    }
}
