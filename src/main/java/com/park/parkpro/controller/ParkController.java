package com.park.parkpro.controller;

import com.park.parkpro.domain.Park;
import com.park.parkpro.dto.PageResponseDto;
import com.park.parkpro.dto.PatchParkRequestDto;
import com.park.parkpro.service.ParkService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parks")
public class ParkController {
    private final ParkService parkService;

    public ParkController(ParkService parkService) {
        this.parkService = parkService;
    }

    @PostMapping
    public ResponseEntity<Park> createPark(@RequestBody Park park) {
        Park createdPark = parkService.createPark(park);
        // Return 201 with Location header
        return ResponseEntity
                .created(URI.create("/api/parks/" + createdPark.getId()))
                .body(createdPark);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<Park>> getAllParks(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Park> parksPage = parkService.getAllParks(name, page, size);
        PageResponseDto<Park> response = new PageResponseDto<>(
                parksPage.getContent(),
                parksPage.getTotalElements(),
                parksPage.getTotalPages(),
                parksPage.getNumber(),
                parksPage.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Park> getParkById(@PathVariable UUID id) {
        Park park = parkService.getParkById(id);
        return ResponseEntity.ok(park);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Park> updatePark(@PathVariable UUID id, @RequestBody Park park) {
        Park updatedPark = parkService.updatePark(id, park);
        return ResponseEntity.ok(updatedPark);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Park> patchPark(@PathVariable UUID id, @RequestBody PatchParkRequestDto patchRequest) {
        Park updatedPark = parkService.patchPark(id, patchRequest);
        return ResponseEntity.ok(updatedPark);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePark(@PathVariable UUID id) {
        parkService.deletePark(id);
        return ResponseEntity.noContent().build();
    }
}
