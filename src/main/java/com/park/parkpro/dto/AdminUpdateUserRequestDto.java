package com.park.parkpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class AdminUpdateUserRequestDto {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 15)
    private String phone;

    @Size(max = 30)
    private String gender;

    @Size(max = 30)
    private String passportNationalId;

    @Size(max = 30)
    private String nationality;

    private Integer age;

    @Size(max = 30)
    private String role;

    private Boolean isActive;

    private Boolean mustResetPassword;

    private UUID parkId;

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
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getMustResetPassword() { return mustResetPassword; }
    public void setMustResetPassword(Boolean mustResetPassword) { this.mustResetPassword = mustResetPassword; }
    public UUID getParkId() { return parkId; }
    public void setParkId(UUID parkId) { this.parkId = parkId; }
}