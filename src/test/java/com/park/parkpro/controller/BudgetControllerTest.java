package com.park.parkpro.controller;

import com.park.parkpro.TestConfig;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateBudgetRequestDto;
import com.park.parkpro.dto.LoginRequestDto;
import com.park.parkpro.dto.BudgetResponseDto;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class BudgetControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ParkRepository parkRepository;

    @Autowired
    private UserRepository userRepository;

    private String financeToken;
    private String governmentToken;
    private String managerToken;
    private String visitorToken;
    private UUID parkId;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        parkRepository.deleteAll();
        userRepository.deleteAll();

        // Seed users
        User financeUser = new User();
        financeUser.setEmail("finance@example.com");
        financeUser.setPassword("$2a$10$..."); // Hashed "financePass123"
        financeUser.setRole("FINANCE_OFFICER");
        financeUser.setFirstName("Finance");
        financeUser.setLastName("Officer");
        userRepository.save(financeUser);

        User govUser = new User();
        govUser.setEmail("gov@example.com");
        govUser.setPassword("$2a$10$..."); // Hashed "govPass123"
        govUser.setRole("GOVERNMENT_OFFICER");
        govUser.setFirstName("Gov");
        govUser.setLastName("Officer");
        userRepository.save(govUser);

        User managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setPassword("$2a$10$..."); // Hashed "managerPass123"
        managerUser.setRole("PARK_MANAGER");
        managerUser.setFirstName("Park");
        managerUser.setLastName("Manager");
        userRepository.save(managerUser);

        User visitorUser = new User();
        visitorUser.setEmail("visitor@example.com");
        visitorUser.setPassword("$2a$10$..."); // Hashed "visitorPass123"
        visitorUser.setRole("VISITOR");
        visitorUser.setFirstName("Visitor");
        visitorUser.setLastName("User");
        userRepository.save(visitorUser);

        // Seed park
        Park park = new Park();
        park.setName("Test Park");
        park.setLocation("Test Location");
        park = parkRepository.save(park);
        parkId = park.getId();

        // Assign park to manager
        managerUser.setPark(park);
        userRepository.save(managerUser);

        // Get tokens
        financeToken = login("finance@example.com", "financePass123");
        governmentToken = login("gov@example.com", "govPass123");
        managerToken = login("manager@example.com", "managerPass123");
        visitorToken = login("visitor@example.com", "visitorPass123");
    }

    private String login(String email, String password) {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail(email);
        login.setPassword(password);
        var response = restTemplate.postForEntity("/login", login, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private HttpEntity<?> createRequest(Object request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @Test
    void createBudgetReturns200() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        var response = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                BudgetResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BudgetResponseDto budget = response.getBody();
        assertNotNull(budget);
        assertEquals(parkId, budget.getParkId());
        assertEquals(2025, budget.getFiscalYear());
        assertEquals(new BigDecimal("10000.00"), budget.getTotalAmount());
        assertEquals("DRAFT", budget.getStatus());
    }

    @Test
    void createDuplicateBudgetReturns400() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                BudgetResponseDto.class
        );

        var response = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Budget already exists"));
    }

    @Test
    void createBudgetUnauthorizedReturns403() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        var response = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, visitorToken),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateBudgetReturns200() {
        CreateBudgetRequestDto createRequest = new CreateBudgetRequestDto();
        createRequest.setFiscalYear(2025);
        createRequest.setTotalAmount(new BigDecimal("10000.00"));
        createRequest.setStatus("DRAFT");

        var createResponse = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(createRequest, financeToken),
                BudgetResponseDto.class
        );
        UUID budgetId = createResponse.getBody().getId();

        CreateBudgetRequestDto updateRequest = new CreateBudgetRequestDto();
        updateRequest.setTotalAmount(new BigDecimal("15000.00"));
        updateRequest.setStatus("DRAFT");

        var response = restTemplate.exchange(
                "/api/budgets/" + budgetId,
                HttpMethod.PATCH,
                createRequest(updateRequest, financeToken),
                BudgetResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BudgetResponseDto budget = response.getBody();
        assertEquals(new BigDecimal("15000.00"), budget.getTotalAmount());
        assertEquals("DRAFT", budget.getStatus());
    }

    @Test
    void approveBudgetReturns200() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        var createResponse = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                BudgetResponseDto.class
        );
        UUID budgetId = createResponse.getBody().getId();

        var response = restTemplate.exchange(
                "/api/budgets/" + budgetId + "/approve",
                HttpMethod.POST,
                createRequest(null, governmentToken),
                BudgetResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BudgetResponseDto budget = response.getBody();
        assertEquals("APPROVED", budget.getStatus());
        assertNotNull(budget.getApprovedAt());
    }

    @Test
    void rejectBudgetReturns200() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        var createResponse = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                BudgetResponseDto.class
        );
        UUID budgetId = createResponse.getBody().getId();

        var response = restTemplate.exchange(
                "/api/budgets/" + budgetId + "/reject",
                HttpMethod.POST,
                createRequest(null, governmentToken),
                BudgetResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BudgetResponseDto budget = response.getBody();
        assertEquals("REJECTED", budget.getStatus());
        assertNotNull(budget.getApprovedAt());
    }

    @Test
    void getBudgetsByParkReturns200() {
        CreateBudgetRequestDto request = new CreateBudgetRequestDto();
        request.setFiscalYear(2025);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setStatus("DRAFT");

        restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.POST,
                createRequest(request, financeToken),
                BudgetResponseDto.class
        );

        var response = restTemplate.exchange(
                "/api/parks/" + parkId + "/budgets",
                HttpMethod.GET,
                createRequest(null, managerToken),
                BudgetResponseDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BudgetResponseDto[] budgets = response.getBody();
        assertEquals(1, budgets.length);
        assertEquals(2025, budgets[0].getFiscalYear());
    }
}