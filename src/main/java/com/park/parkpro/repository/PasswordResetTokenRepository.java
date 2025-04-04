// src/main/java/com/park/parkpro/repository/PasswordResetTokenRepository.java
package com.park.parkpro.repository;

import com.park.parkpro.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
}