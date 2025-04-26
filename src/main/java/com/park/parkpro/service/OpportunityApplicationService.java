package com.park.parkpro.service;

import com.park.parkpro.domain.Opportunity;
import com.park.parkpro.domain.OpportunityApplication;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.OpportunityApplicationRepository;
import com.park.parkpro.repository.OpportunityRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class OpportunityApplicationService {
    private final OpportunityApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OpportunityApplicationService(
            OpportunityApplicationRepository applicationRepository,
            OpportunityRepository opportunityRepository,
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public OpportunityApplication createApplication(
            UUID opportunityId, String firstName, String lastName, String email, String applicationLetterUrl, String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User applicant = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity not found with ID: " + opportunityId));

        OpportunityApplication application = new OpportunityApplication();
        application.setOpportunity(opportunity);
        application.setFirstName(firstName);
        application.setLastName(lastName);
        application.setEmail(email);
        application.setApplicationLetterUrl(applicationLetterUrl);
        application.setStatus("SUBMITTED");

        return applicationRepository.save(application);
    }

    @Transactional
    public OpportunityApplication updateApplicationStatus(UUID applicationId, String status, String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        OpportunityApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));

        if (!Arrays.asList("SUBMITTED", "REVIEWED", "ACCEPTED", "REJECTED").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public List<OpportunityApplication> getApplicationsByOpportunity(UUID opportunityId, String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        return applicationRepository.findByOpportunityId(opportunityId);
    }

    public OpportunityApplication getApplicationById(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));
    }

    public List<OpportunityApplication> getMyApplications(String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        return applicationRepository.findByEmail(userEmail);
    }

    public List<OpportunityApplication> getAllApplications(String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        if (!Arrays.asList("ADMIN", "PARK_MANAGER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or PARK_MANAGER can view all applications");
        }

        return applicationRepository.findAll();
    }

    public List<OpportunityApplication> getApplicationsByPark(UUID parkId, String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        if (!Arrays.asList("ADMIN", "PARK_MANAGER", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN FINANCE_OFFICER or PARK_MANAGER can view applications by park");
        }

        return applicationRepository.findByParkId(parkId);
    }
}