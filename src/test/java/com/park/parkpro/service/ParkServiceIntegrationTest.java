package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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
        Park park1 = new Park("Loango", "Southwest Gabon", "Coastal park");
        parkService.createPark(park1);

        Park park2 = new Park("Loango", "Southwest Gabon", "Coastal park");

        // Act & Assert
        DuplicateParkException exception = assertThrows(DuplicateParkException.class, () -> parkService.createPark(park2));
        assertEquals("Park with name 'Loango' already exists", exception.getMessage());
    }
}
