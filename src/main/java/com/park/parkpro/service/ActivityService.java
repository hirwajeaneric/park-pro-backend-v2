package com.park.parkpro.service;

import com.park.parkpro.domain.Activity;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.ActivityRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ActivityService(ActivityRepository activityRepository, ParkRepository parkRepository,
                           UserRepository userRepository, JwtUtil jwtUtil) {
        this.activityRepository = activityRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Activity createActivity(UUID parkId, String name, BigDecimal price, String description,
                                   Integer capacityPerDay, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can create activities");
        }
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        if ("PARK_MANAGER".equals(user.getRole()) && !park.getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only create activities for their assigned park");
        }

        Activity activity = new Activity();
        activity.setName(name);
        activity.setPark(park);
        activity.setPrice(price);
        activity.setDescription(description);
        activity.setCapacityPerDay(capacityPerDay);
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity updateActivity(UUID activityId, String name, BigDecimal price, String description,
                                   Integer capacityPerDay, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can update activities");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + activityId));
        if ("PARK_MANAGER".equals(user.getRole()) && !activity.getPark().getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only update activities in their assigned park");
        }

        if (name != null && !name.trim().isEmpty()) activity.setName(name);
        if (price != null) activity.setPrice(price);
        if (description != null) activity.setDescription(description);
        if (capacityPerDay != null) activity.setCapacityPerDay(capacityPerDay);
        activity.setUpdatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(UUID activityId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can delete activities");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + activityId));
        if ("PARK_MANAGER".equals(user.getRole()) && !activity.getPark().getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only delete activities in their assigned park");
        }

        activityRepository.delete(activity);
    }

    public List<Activity> getActivitiesByPark(UUID parkId) {
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }
        return activityRepository.findByParkId(parkId);
    }

    public Activity getActivityById(UUID activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + activityId));
    }
}