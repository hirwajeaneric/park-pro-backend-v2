package com.park.parkpro.service;

import com.park.parkpro.domain.Opportunity;
import com.park.parkpro.domain.OpportunityApplication;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.OpportunityApplicationRepository;
import com.park.parkpro.repository.OpportunityRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OpportunityApplicationService {
    private final OpportunityApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final JwtUtil jwtUtil;

    public OpportunityApplicationService(OpportunityApplicationRepository applicationRepository,
                                         OpportunityRepository opportunityRepository, JwtUtil jwtUtil) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public OpportunityApplication createApplication(UUID opportunityId, String firstName, String lastName,
                                                    String email, String applicationLetterUrl, String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity not found with ID: " + opportunityId));
        if (!"OPEN".equals(opportunity.getStatus())) {
            throw new BadRequestException("Cannot apply to a closed opportunity");
        }

        OpportunityApplication application = new OpportunityApplication();
        application.setOpportunity(opportunity);
        application.setFirstName(firstName);
        application.setLastName(lastName);
        application.setEmail(email); // Could enforce userEmail if desired
        application.setApplicationLetterUrl(applicationLetterUrl);
        application.setStatus("SUBMITTED");
        return applicationRepository.save(application);
    }

    @Transactional
    public OpportunityApplication updateApplicationStatus(UUID applicationId, String status, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        OpportunityApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));
        Opportunity opportunity = application.getOpportunity();
        User user = opportunity.getCreatedBy(); // Creator of the opportunity

        if (!List.of("ADMIN", "PARK_MANAGER").contains(user.getRole()) ||
                ("PARK_MANAGER".equals(user.getRole()) && !user.getEmail().equals(email))) {
            throw new ForbiddenException("Only the opportunity creator or ADMIN can update application status");
        }

        if (!List.of("SUBMITTED", "REVIEWED", "ACCEPTED", "REJECTED").contains(status)) {
            throw new BadRequestException("Invalid status value");
        }

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public List<OpportunityApplication> getApplicationsByOpportunity(UUID opportunityId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity not found with ID: " + opportunityId));
        User creator = opportunity.getCreatedBy();

        if (!creator.getEmail().equals(email) && !"ADMIN".equals(creator.getRole())) {
            throw new ForbiddenException("Only the opportunity creator or ADMIN can view applications");
        }

        return applicationRepository.findByOpportunityId(opportunityId);
    }

    public OpportunityApplication getApplicationById(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));
    }

    public List<OpportunityApplication> getMyApplications(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        return applicationRepository.findByEmail(email);
    }
}