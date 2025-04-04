package com.park.parkpro.repository;

import com.park.parkpro.domain.User;
import com.park.parkpro.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenAndUser(String token, User user);
    Optional<VerificationToken> findByUser(User user);
}