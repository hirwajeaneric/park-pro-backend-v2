package com.park.parkpro.service;

import com.park.parkpro.domain.Park;
import com.park.parkpro.dto.PatchParkRequestDto;
import com.park.parkpro.exception.DuplicateParkException;
import com.park.parkpro.repository.ParkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<Park> getAllParks(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null && !name.trim().isEmpty()) {
            return parkRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        }
        return parkRepository.findAll(pageable);
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

    public Park patchPark(UUID id, PatchParkRequestDto patchRequest) {
        Park existingPark = parkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Park with ID '" + id + "' not found"));

        if (patchRequest.getName() != null && !patchRequest.getName().trim().isEmpty()) {
            parkRepository.findByName(patchRequest.getName())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new DuplicateParkException("Park with name '" + patchRequest.getName() + "' already exists");
                        }
                    });
            existingPark.setName(patchRequest.getName().trim());
        }
        if (patchRequest.getLocation() != null && !patchRequest.getLocation().trim().isEmpty()) {
            existingPark.setLocation(patchRequest.getLocation().trim());
        }
        if (patchRequest.getDescription() != null) {
            existingPark.setDescription(patchRequest.getDescription());
        }
        existingPark.setUpdatedAt(LocalDateTime.now());
        return parkRepository.save(existingPark);
    }

    public void deletePark(UUID id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Park with ID '" + id + "' not found"));
        parkRepository.delete(park);
    }
}
