package com.park.parkpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/health", "/actuator/health").permitAll() // Allow health check without auth
                        .requestMatchers("/api/parks", "/api/parks/**").authenticated() // Request auth for parks API
//                        .anyRequest().authenticated() // Default: Any other endpoints require auth
//                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Temporary in-memory user for testing
        var user = User.withUsername("admin")
                .password("{noop}adminpass}") // {noop} means no password encoding (for simplicity)
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
