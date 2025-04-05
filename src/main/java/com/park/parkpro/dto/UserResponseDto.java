package com.park.parkpro.dto;

import java.util.UUID;

public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private UUID parkId;
    private String phone;
    private String gender;
    private String passportNationalId;
    private String nationality;
    private int age;

    public UserResponseDto(UUID id, String firstName, String lastName, String email, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public UserResponseDto(UUID id, String firstName, String lastName, String email, String role, UUID parkId, String phone, String gender, String passportNationalId, String nationality, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.parkId = parkId;
        this.phone = phone;
        this.gender = gender;
        this.passportNationalId = passportNationalId;
        this.nationality = nationality;
        this.age = age;
    }

    // Getters
    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public UUID getParkId() { return parkId; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getPassportNationalId() { return passportNationalId; }
    public String getNationality() { return nationality; }
    public int getAge() { return age; }
}