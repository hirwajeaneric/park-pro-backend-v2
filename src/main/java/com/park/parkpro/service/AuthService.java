package com.park.parkpro.service;

import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.security.JwtUtil;
import com.park.parkpro.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            String role = userDetails.getAuthorities().iterator().next().getAuthority().substring(5); // Remove "ROLE_"
            return jwtUtil.generateToken(email, role);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}