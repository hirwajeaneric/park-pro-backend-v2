package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ParkService {
    private final ParkRepository parkRepository;

    public ParkService(ParkRepository parkRepository) {
        this.parkRepository = parkRepository;
    }

    public Park createPark(Park park) {
        // Validate inputs
        if (park.getName() == null || park.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Park name cannot be empty");
        }
        if (park.getLocation() == null || park.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Park location cannot be empty");
        }

        // Check for duplicate name
        if (parkRepository.findByName(park.getName()).isPresent()) {
            throw new DuplicateParkException("Park with name '" + park.getName() + "' already exists");
        }
        // Save the park
        return parkRepository.save(park);
    }

    public List<Park> getAllParks() {
        return parkRepository.findAll();
    }

    public Park getParkById(UUID id) {
        return parkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Park with id '" + id + "' does not exist"));
    }

    public Park updatePark(UUID id, Park updatedPark) {
        Park existingPark = parkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Park with id '" + id + "' does not exist"));

        // Check for duplicate name (excluding the current park)
        parkRepository.findByName(updatedPark.getName())
                .ifPresent(p -> {
                    if (!p.getId().equals(id)) {
                        throw new DuplicateParkException("Park with name '" + updatedPark.getName() + "' already exists");
                    }
                });

        existingPark.setName(updatedPark.getName());
        existingPark.setLocation(updatedPark.getLocation());
        existingPark.setDescription(updatedPark.getDescription());
        existingPark.setUpdatedAt(LocalDateTime.now());
        return parkRepository.save(existingPark);
    }

    public void deletePark(UUID id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Park with ID '" + id + "' not found"));
        parkRepository.delete(park);
    }
}
