package dev.rangel.statkick.integration.footballapi.resilience;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.port.FootballMatchPort;
import dev.rangel.statkick.integration.footballapi.exception.ExternalServiceUnavailableException;
import dev.rangel.statkick.integration.footballapi.exception.RateLimitExceededException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Component("resilientFootballMatchAdapter")
public class ResilientFootballMatchAdapter implements FootballMatchPort {

    private final FootballMatchPort delegate;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;

    public ResilientFootballMatchAdapter(
            @Qualifier("footballDataClient") FootballMatchPort delegate,
            RateLimiterRegistry rateLimiterRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiterRegistry.rateLimiter("footballApi");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("footballApi");
    }

    @Override
    public List<Match> fetchMatches(String competitionCode) {
        try {
            return rateLimiter.executeSupplier(() ->
                    circuitBreaker.executeSupplier(() ->
                            delegate.fetchMatches(competitionCode)
                    )
            );
        }
        // Resilience4j does not expose the exact time remaining until the next cycle.
        // We use the total window size (e.g., 1 min) as a safe upper bound
        // to inform the caller, avoiding the creation of magic numbers in the code.
        catch (RequestNotPermitted e) {
            Duration waitCeiling = rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod();
            throw new RateLimitExceededException(
                    "Rate limit exceeded locally before hitting Football API",
                    waitCeiling
            );
        } catch (CallNotPermittedException e) {
            throw new ExternalServiceUnavailableException(
                    "Football API circuit is OPEN. Call blocked to prevent cascading failures.", e
            );
        }
    }
}