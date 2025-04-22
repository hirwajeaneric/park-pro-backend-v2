// src/main/java/com/park/parkpro/service/OpportunityService.java
package com.park.parkpro.service;

import com.park.parkpro.domain.Opportunity;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.OpportunityRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final ParkRepository parkRepository;
    private final JwtUtil jwtUtil;

    public OpportunityService(OpportunityRepository opportunityRepository, UserRepository userRepository,
                              ParkRepository parkRepository, JwtUtil jwtUtil) {
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.parkRepository = parkRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Opportunity createOpportunity(String title, String description, String details, String type,
                                         String status, String visibility, UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        if ("PARK_MANAGER".equals(creator.getRole()) &&
                (creator.getPark() == null || !creator.getPark().getId().equals(parkId))) {
            throw new ForbiddenException("PARK_MANAGER can only create opportunities for their assigned park");
        }

        Opportunity opportunity = new Opportunity();
        opportunity.setTitle(title);
        opportunity.setDescription(description);
        opportunity.setDetails(details);
        opportunity.setType(type);
        opportunity.setStatus(status);
        opportunity.setVisibility(visibility);
        opportunity.setCreatedBy(creator);
        opportunity.setPark(park);
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public Opportunity updateOpportunity(UUID opportunityId, String title, String description, String details,
                                         String status, String visibility, UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity not found with ID: " + opportunityId));

        if (parkId != null) {
            Park park = parkRepository.findById(parkId)
                    .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
            if ("PARK_MANAGER".equals(user.getRole()) &&
                    (user.getPark() == null || !user.getPark().getId().equals(parkId))) {
                throw new ForbiddenException("PARK_MANAGER can only update opportunities for their assigned park");
            }
            opportunity.setPark(park);
        }
        if (title != null) opportunity.setTitle(title);
        if (description != null) opportunity.setDescription(description);
        if (details != null) opportunity.setDetails(details);
        if (status != null) opportunity.setStatus(status);
        if (visibility != null) opportunity.setVisibility(visibility);
        opportunity.setUpdatedAt(LocalDateTime.now());
        return opportunityRepository.save(opportunity);
    }

    public List<Opportunity> getAllOpportunities(String token) {
        if (token == null || token.isEmpty()) {
            return opportunityRepository.findByVisibility("PUBLIC");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (List.of("ADMIN", "PARK_MANAGER").contains(user.getRole())) {
            return opportunityRepository.findAll();
        }
        return opportunityRepository.findByVisibility("PUBLIC");
    }

    public Opportunity getOpportunityById(UUID opportunityId, String token) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity not found with ID: " + opportunityId));

        if (token == null || token.isEmpty()) {
            if (!"PUBLIC".equals(opportunity.getVisibility())) {
                throw new ForbiddenException("This opportunity is private and requires authentication");
            }
            return opportunity;
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (List.of("ADMIN", "PARK_MANAGER").contains(user.getRole())) {
            return opportunity;
        }
        if (!"PUBLIC".equals(opportunity.getVisibility())) {
            throw new ForbiddenException("This opportunity is private and restricted to authorized users");
        }
        return opportunity;
    }

    public List<Opportunity> getOpportunitiesByCreator(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "PARK_MANAGER").contains(creator.getRole())) {
            throw new ForbiddenException("Only ADMIN or PARK_MANAGER can view their created opportunities");
        }
        return opportunityRepository.findByCreatedById(creator.getId());
    }

    // src/main/java/com/park/parkpro/service/OpportunityService.java (partial update)
    public List<Opportunity> getOpportunitiesByParkId(UUID parkId, String token) {
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }

        if (token == null || token.isEmpty()) {
            // Unauthenticated: return only PUBLIC opportunities for this park
            return opportunityRepository.findByParkIdAndVisibility(parkId, "PUBLIC");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if ("ADMIN".equals(user.getRole())) {
            // ADMIN: return all opportunities for this park
            return opportunityRepository.findByParkId(parkId);
        }
        if ("PARK_MANAGER".equals(user.getRole())) {
            // PARK_MANAGER: return all opportunities for this park if it's their assigned park
            if (user.getPark() == null || !user.getPark().getId().equals(parkId)) {
                throw new ForbiddenException("PARK_MANAGER can only view opportunities for their assigned park");
            }
            return opportunityRepository.findByParkId(parkId);
        }
        // Other authenticated users: return only PUBLIC opportunities for this park
        return opportunityRepository.findByParkIdAndVisibility(parkId, "PUBLIC");
    }
}