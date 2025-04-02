package com.park.parkpro.config;

import com.park.parkpro.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/health", "/login", "/api/signup").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/debug/auth").authenticated()
                        .requestMatchers("/api/parks/{parkId}/budgets").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers("/api/budgets/{budgetId}").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers("/api/budgets/{budgetId}/approve").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/reject").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/parks/{parkId}/budgets/**").hasAnyRole("ADMIN", "FINANCE_OFFICER", "PARK_MANAGER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers("/api/parks/**").hasAnyRole("ADMIN", "PARK_MANAGER", "FINANCE_MANAGER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}