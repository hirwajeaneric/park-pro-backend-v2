package com.park.parkpro.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String passportNationalId;
    private String nationality;
    private Integer age;
    private String role;
    private UUID parkId;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponseDto(UUID id, String firstName, String lastName, String email, String phone, String gender,
                           String passportNationalId, String nationality, Integer age, String role, UUID parkId,
                           boolean isActive, LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.passportNationalId = passportNationalId;
        this.nationality = nationality;
        this.age = age;
        this.role = role;
        this.parkId = parkId;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getPassportNationalId() { return passportNationalId; }
    public String getNationality() { return nationality; }
    public Integer getAge() { return age; }
    public String getRole() { return role; }
    public UUID getParkId() { return parkId; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}