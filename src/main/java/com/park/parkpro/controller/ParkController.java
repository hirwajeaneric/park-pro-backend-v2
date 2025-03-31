package com.park.parkpro.controller;

import com.park.parkpro.domain.Park;
import com.park.parkpro.service.ParkService;
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
    public ResponseEntity<List<Park>> getAllParks() {
        List<Park> parks = parkService.getAllParks();
        return ResponseEntity.ok(parks);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePark(@PathVariable UUID id) {
        parkService.deletePark(id);
        return ResponseEntity.noContent().build();
    }
}
