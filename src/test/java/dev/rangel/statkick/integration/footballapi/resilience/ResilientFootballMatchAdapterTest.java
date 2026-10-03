package dev.rangel.statkick.integration.footballapi.resilience;

import dev.rangel.statkick.domain.port.FootballMatchPort;
import dev.rangel.statkick.integration.footballapi.exception.ExternalServiceUnavailableException;
import dev.rangel.statkick.integration.footballapi.exception.RateLimitExceededException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// TODO: Tech Debt - Remove @SpringBootTest and instantiate Resilience4j objects programmatically.
// Loading the full Spring context is unnecessarily heavy for this unit test and risks state leakage
// (e.g., shared CircuitBreaker state or consumed RateLimiter permissions) across different test suites.
// Future refactor: instantiate CircuitBreaker and RateLimiter using plain Java (e.g., CircuitBreaker.of())
// and pass them along with a mocked FootballMatchPort directly via the adapter's constructor.
@SpringBootTest
class ResilientFootballMatchAdapterTest {

    @Autowired
    @Qualifier("resilientFootballMatchAdapter")
    private FootballMatchPort resilientFootballMatchAdapter;

    @MockitoBean(name = "footballDataClient")
    private FootballMatchPort mockClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    private CircuitBreaker circuitBreaker;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("footballApi");
        rateLimiter = rateLimiterRegistry.rateLimiter("footballApi");

        circuitBreaker.transitionToClosedState();
        reset(mockClient);
    }

    @Test
    @DisplayName("Should block calls and throw custom exception when Circuit is OPEN")
    void shouldBlockCallsWhenCircuitIsOpen() {
        circuitBreaker.transitionToOpenState();

        assertThrows(ExternalServiceUnavailableException.class,
                () -> resilientFootballMatchAdapter.fetchMatches("PL"));

        verify(mockClient, never()).fetchMatches(anyString());
    }

    @Test
    @DisplayName("Should allow the 3 test calls in HALF_OPEN and close the circuit if successful")
    void shouldAllowLimitedCallsAndCloseWhenHalfOpen() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();

        when(mockClient.fetchMatches("PL")).thenReturn(Collections.emptyList());

        resilientFootballMatchAdapter.fetchMatches("PL");
        resilientFootballMatchAdapter.fetchMatches("PL");
        resilientFootballMatchAdapter.fetchMatches("PL");

        verify(mockClient, times(3)).fetchMatches("PL");

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    @DisplayName("Should block local calls when the Rate Limiter has no permissions left")
    void shouldBlockCallWhenRateLimiterExhausted() {
        int availablePermissions = rateLimiter.getMetrics().getAvailablePermissions();

        when(mockClient.fetchMatches("SA")).thenReturn(Collections.emptyList());

        for (int i = 0; i < availablePermissions; i++) {
            resilientFootballMatchAdapter.fetchMatches("SA");
        }

        assertThrows(RateLimitExceededException.class,
                () -> resilientFootballMatchAdapter.fetchMatches("SA"));

        verify(mockClient, times(availablePermissions)).fetchMatches("SA");
    }
}