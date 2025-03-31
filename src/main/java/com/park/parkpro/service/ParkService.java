package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
