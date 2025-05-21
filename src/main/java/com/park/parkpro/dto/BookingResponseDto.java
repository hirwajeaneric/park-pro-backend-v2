package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingResponseDto {
    private UUID id;
    private UUID visitorId;
    private UUID activityId;
    private BigDecimal amount;
    private UUID parkId;
    private LocalDate visitDate;
    private String status;
    private String paymentReference;
    private String currency;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer numberOfTickets;
    private List<GroupMemberResponseDto> groupMembers;

    public BookingResponseDto(UUID id, UUID visitorId, UUID activityId, BigDecimal amount, UUID parkId,
                              LocalDate visitDate, String status, String paymentReference, String currency,
                              LocalDateTime confirmedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                              Integer numberOfTickets, List<GroupMemberResponseDto> groupMembers) {
        this.id = id;
        this.visitorId = visitorId;
        this.activityId = activityId;
        this.amount = amount;
        this.parkId = parkId;
        this.visitDate = visitDate;
        this.status = status;
        this.paymentReference = paymentReference;
        this.currency = currency;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.numberOfTickets = numberOfTickets;
        this.groupMembers = groupMembers;
    }

    public static class GroupMemberResponseDto {
        private UUID userId;
        private String guestName;
        private String guestEmail;

        public GroupMemberResponseDto(UUID userId, String guestName, String guestEmail) {
            this.userId = userId;
            this.guestName = guestName;
            this.guestEmail = guestEmail;
        }

        // Getters
        public UUID getUserId() { return userId; }
        public String getGuestName() { return guestName; }
        public String getGuestEmail() { return guestEmail; }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getVisitorId() { return visitorId; }
    public UUID getActivityId() { return activityId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getParkId() { return parkId; }
    public LocalDate getVisitDate() { return visitDate; }
    public String getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public String getCurrency() { return currency; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getNumberOfTickets() { return numberOfTickets; }
    public List<GroupMemberResponseDto> getGroupMembers() { return groupMembers; }
}