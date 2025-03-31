package com.park.parkpro.controller;

import com.park.parkpro.domain.Park;
import com.park.parkpro.service.ParkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
}
