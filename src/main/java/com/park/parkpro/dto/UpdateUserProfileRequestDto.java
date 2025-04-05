package com.park.parkpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequestDto {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be 50 characters or less")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be 50 characters or less")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be 100 characters or less")
    private String email;

    @Size(max = 15, message = "Phone must be 15 characters or less")
    private String phone;

    @Size(max = 30, message = "Gender must be 30 characters or less")
    private String gender;

    @Size(max = 30, message = "Passport/National ID must be 30 characters or less")
    private String passportNationalId;

    @Size(max = 30, message = "Nationality must be 30 characters or less")
    private String nationality;

    private Integer age; // Nullable, no strict validation beyond database constraints

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPassportNationalId() { return passportNationalId; }
    public void setPassportNationalId(String passportNationalId) { this.passportNationalId = passportNationalId; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}