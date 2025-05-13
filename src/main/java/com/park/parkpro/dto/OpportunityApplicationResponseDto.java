package com.park.parkpro.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OpportunityApplicationResponseDto {
    private UUID id;
    private UUID opportunityId;
    private String firstName;
    private String lastName;
    private String email;
    private String applicationLetterUrl;
    private String status;
    private String approvalMessage;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OpportunityApplicationResponseDto(UUID id, UUID opportunityId, String firstName, String lastName,
                                             String email, String applicationLetterUrl, String status,
                                             String approvalMessage, String rejectionReason,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.opportunityId = opportunityId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.applicationLetterUrl = applicationLetterUrl;
        this.status = status;
        this.approvalMessage = approvalMessage;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getOpportunityId() { return opportunityId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getApplicationLetterUrl() { return applicationLetterUrl; }
    public String getStatus() { return status; }
    public String getApprovalMessage() { return approvalMessage; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}