package com.park.parkpro.controller;

import com.park.parkpro.domain.Activity;
import com.park.parkpro.dto.ActivityResponseDto;
import com.park.parkpro.dto.CreateActivityRequestDto;
import com.park.parkpro.dto.UpdateActivityRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/parks/{parkId}/activities")
    public ResponseEntity<ActivityResponseDto> createActivity(
            @PathVariable UUID parkId,
            @Valid @RequestBody CreateActivityRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Activity activity = activityService.createActivity(parkId, request.getName(), request.getPrice(),
                request.getDescription(), request.getCapacityPerDay(), token);
        return ResponseEntity.created(URI.create("/api/activities/" + activity.getId()))
                .body(mapToActivityDto(activity));
    }

    @PatchMapping("/activities/{activityId}")
    public ResponseEntity<ActivityResponseDto> updateActivity(
            @PathVariable UUID activityId,
            @RequestBody UpdateActivityRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Activity activity = activityService.updateActivity(activityId, request.getName(), request.getPrice(),
                request.getDescription(), request.getCapacityPerDay(), token);
        return ResponseEntity.ok(mapToActivityDto(activity));
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable UUID activityId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        activityService.deleteActivity(activityId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parks/{parkId}/activities")
    public ResponseEntity<List<ActivityResponseDto>> getActivitiesByPark(@PathVariable UUID parkId) {
        List<Activity> activities = activityService.getActivitiesByPark(parkId);
        return ResponseEntity.ok(activities.stream().map(this::mapToActivityDto).collect(Collectors.toList()));
    }

    @GetMapping("/activities/{activityId}")
    public ResponseEntity<ActivityResponseDto> getActivityById(@PathVariable UUID activityId) {
        Activity activity = activityService.getActivityById(activityId);
        return ResponseEntity.ok(mapToActivityDto(activity));
    }

    private ActivityResponseDto mapToActivityDto(Activity activity) {
        return new ActivityResponseDto(activity.getId(), activity.getName(), activity.getPark().getId(),
                activity.getPrice(), activity.getDescription(), activity.getCapacityPerDay(),
                activity.getCreatedAt(), activity.getUpdatedAt());
    }
}