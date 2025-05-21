package com.park.parkpro.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateBookingRequestDto {
    @NotNull
    private UUID activityId;

    @NotNull
    @FutureOrPresent
    private LocalDate visitDate;

    @NotNull
    private Integer numberOfTickets;

    @Size(min = 0)
    private List<GroupMemberDto> groupMembers;

    public static class GroupMemberDto {
        private UUID userId; // Optional, for registered users
        private String guestName; // For non-registered guests
        private String guestEmail; // For non-registered guests

        // Getters and Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }
        public String getGuestEmail() { return guestEmail; }
        public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    }

    // Getters and Setters
    public UUID getActivityId() { return activityId; }
    public void setActivityId(UUID activityId) { this.activityId = activityId; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public Integer getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(Integer numberOfTickets) { this.numberOfTickets = numberOfTickets; }
    public List<GroupMemberDto> getGroupMembers() { return groupMembers; }
    public void setGroupMembers(List<GroupMemberDto> groupMembers) { this.groupMembers = groupMembers; }
}