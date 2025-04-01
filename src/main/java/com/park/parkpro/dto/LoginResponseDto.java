package com.park.parkpro.dto;

import java.util.UUID;

public class LoginResponseDto {
    private String token;
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private UUID parkId;

    public LoginResponseDto(String token, UUID id, String email, String firstName, String lastName, String role, UUID parkId) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.parkId = parkId;
    }

    // Getters
    public String getToken() { return token; }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public UUID getParkId() { return parkId; }
}