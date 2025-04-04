// src/main/java/com/park/parkpro/service/AuthService.java
package com.park.parkpro.service;

import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import com.park.parkpro.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       UserDetailsServiceImpl userDetailsService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            if (!user.isActive()) {
                throw new UnauthorizedException("Account is not active. Please verify your email.");
            }
            if (user.isMustResetPassword()) {
                throw new UnauthorizedException("Password reset required. Please reset your password.");
            }
            String role = userDetails.getAuthorities().iterator().next().getAuthority().substring(5); // Remove "ROLE_"
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return jwtUtil.generateToken(email, role);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}