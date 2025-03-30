package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParkServiceTest {
    @Mock
    private ParkRepository parkRepository;

    @InjectMocks
    private ParkService parkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void shouldCreateParkSuccessfully() {
        // Arrange
        Park park = new Park("Loango", "Southwest Gabon", "Coastal park");
        when(parkRepository.findByName("Loango"))
                .thenReturn(Optional.empty());
        when(parkRepository.save(any(Park.class)))
                .thenReturn(new Park("Loango", "Southwest Gabon", "Coastal park"));

        // Act
        Park result = parkService.createPark(park);

        // Assert
        assertNotNull(result);
        assertEquals("Loango", result.getName());
        assertEquals("Southwest Gabon", result.getLocation());
        verify(parkRepository, times(1)).save(park);
    }

    @Test
    void shouldThrowExceptionForDuplicateParkName() {
        // Arrange
        Park existingPark = new Park("Loango", "Southwest Gabon", "Coastal park");
        Park newPark = new Park("Loango", "Southwest Gabon", "Coastal park");
        when(parkRepository.findByName("Loango"))
                .thenReturn(Optional.of(existingPark));

        // Act & Assert
        DuplicateParkException exception = assertThrows(DuplicateParkException.class, () -> {
            parkService.createPark(newPark);
        });
        assertEquals("Park with name 'Loango' already exists", exception.getMessage());
        verify(parkRepository, never()).save(any(Park.class));
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        Park park = new Park("", "Southwest Gabon", "Coastal park");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            parkService.createPark(park);
        });
        assertEquals("Park name cannot be empty", exception.getMessage());
    }
}
