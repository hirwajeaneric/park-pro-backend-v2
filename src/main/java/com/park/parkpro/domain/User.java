package com.park.parkpro.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "\"user\"")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String role;

    @ManyToOne
    @JoinColumn(name = "park_id")
    private Park park;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(name="user_park", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "park_id"))
    private Set<Park> parks = new HashSet<>();

    public User() {}

    // Add method to manage the relationship
    public void addPark(Park park) {
        this.parks.add(park);
        park.getUsers().add(this);
    }

    public void removePark(Park park) {
        this.parks.remove(park);
        park.getUsers().remove(this);
    }
}