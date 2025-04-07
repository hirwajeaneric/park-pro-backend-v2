package com.park.parkpro.config;

import com.park.parkpro.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/health", "/api/login", "/api/signup", "/api/verify-account", "/api/password-reset/**", "/api/new-verification-code").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/{userId}").authenticated() // View profile
                        .requestMatchers(HttpMethod.PATCH, "/api/users/{userId}").authenticated() // Update profile
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Other user endpoints restricted to ADMIN
                        .requestMatchers("/api/users", "/api/users/**").hasAnyRole("ADMIN", "FINANCE_MANAGER")
                        .requestMatchers("/api/debug/auth").authenticated()
                        .requestMatchers("/api/parks/{parkId}/budgets").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers("/api/budgets/{budgetId}").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers("/api/budgets/{budgetId}/approve").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/reject").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/categories").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "PARK_MANAGER", "AUDITOR") // GET
                        .requestMatchers(HttpMethod.POST, "/api/budgets/{budgetId}/categories").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers(HttpMethod.PATCH, "/api/budgets/{budgetId}/categories/{categoryId}").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers(HttpMethod.DELETE, "/api/budgets/{budgetId}/categories/{categoryId}").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers(HttpMethod.POST, "/api/budgets/{budgetId}/expenses").hasAnyRole("ADMIN", "FINANCE_OFFICER", "PARK_MANAGER")
                        .requestMatchers("/api/budgets/{budgetId}/expenses/{expenseId}/approve").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/expenses/{expenseId}/reject").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/categories/{categoryId}/expenses").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR", "PARK_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/budgets/{budgetId}/withdraw-requests").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/budgets/{budgetId}/withdraw-requests/{withdrawRequestId}/approve").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/withdraw-requests/{withdrawRequestId}/reject").hasAnyRole("ADMIN", "FINANCE_OFFICER")
                        .requestMatchers("/api/budgets/{budgetId}/categories/{categoryId}/withdraw-requests").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/parks/{parkId}/funding-requests").hasRole("FINANCE_OFFICER")
                        .requestMatchers("/api/funding-requests/{fundingRequestId}/approve").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/funding-requests/{fundingRequestId}/reject").hasRole("GOVERNMENT_OFFICER")
                        .requestMatchers("/api/parks/{parkId}/funding-requests").hasAnyRole("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/parks/{parkId}/activities").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/activities/{activityId}").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/activities/{activityId}").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/parks/{parkId}/activities", "/api/activities/{activityId}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("VISITOR")
                        .requestMatchers("/api/bookings/my").hasRole("VISITOR")
                        .requestMatchers("/api/bookings/{bookingId}/confirm").hasAnyRole("PARK_MANAGER")
                        .requestMatchers("/api/bookings/{bookingId}/cancel").hasAnyRole("PARK_MANAGER", "VISITOR")
                        .requestMatchers("/api/parks/{parkId}/bookings").hasAnyRole("PARK_MANAGER", "FINANCE_OFFICER")
                        .requestMatchers("/api/bookings/{bookingId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/donations").hasRole("VISITOR")
                        .requestMatchers("/api/donations/my").hasRole("VISITOR")
                        .requestMatchers("/api/donations/{donationId}/confirm").hasAnyRole("PARK_MANAGER", "ADMIN")
                        .requestMatchers("/api/donations/{donationId}/cancel").hasAnyRole("VISITOR", "PARK_MANAGER", "FINANCE_OFFICER")
                        .requestMatchers("/api/parks/{parkId}/donations").hasAnyRole("PARK_MANAGER", "ADMIN", "FINANCE_OFFICER", "AUDITOR")
                        .requestMatchers("/api/donations/{donationId}").authenticated()
                        .requestMatchers("/api/parks/**").hasAnyRole("ADMIN", "PARK_MANAGER", "GOVERNMENT_OFFICER", "AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/opportunities").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/opportunities/{opportunityId}").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/opportunities/my").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/opportunities", "/api/opportunities/{opportunityId}", "/api/park/{parkId}/opportunities").permitAll() // Public access
                        .requestMatchers(HttpMethod.POST, "/api/opportunity-applications").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/opportunity-applications/{applicationId}/status").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/opportunity-applications/opportunity/{opportunityId}").hasAnyRole("ADMIN", "PARK_MANAGER")
                        .requestMatchers("/api/opportunity-applications/{applicationId}", "/api/opportunity-applications/my").authenticated()
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins for development (use specific origins in production)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        // Allow headers like Authorization and Content-Type
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // Allow credentials (e.g., cookies, Authorization headers) if needed
        configuration.setAllowCredentials(true);
        // Set max age for CORS preflight cache
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all endpoints
        return source;
    }
}