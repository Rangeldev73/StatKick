package dev.rangel.statkick.integration.footballapi.client;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.integration.footballapi.dto.MatchesResponseDto;
import dev.rangel.statkick.integration.footballapi.exception.ExternalServiceUnavailableException;
import dev.rangel.statkick.integration.footballapi.exception.ExternalTimeoutException;
import dev.rangel.statkick.integration.footballapi.exception.IntegrationException;
import dev.rangel.statkick.integration.footballapi.exception.RateLimitExceededException;
import dev.rangel.statkick.integration.footballapi.mapper.MatchMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class FootballDataClient {

    private final RestClient restClient;
    private final MatchMapper matchMapper;

    public FootballDataClient(RestClient footballApiClient, MatchMapper matchMapper) {
        this.restClient = footballApiClient;
        this.matchMapper = matchMapper;
    }

    public List<Match> fetchMatches(String competitionCode) {
        try {
            MatchesResponseDto response = restClient.get()
                    .uri("/competitions/{code}/matches", competitionCode)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {
                        if (httpResponse.getStatusCode().value() == 429) {
                            String retryHeader = httpResponse.getHeaders().getFirst("Retry-After");
                            long seconds = (retryHeader != null) ? Long.parseLong(retryHeader) : 60L;
                            throw new RateLimitExceededException(
                                    "Rate limit exceeded for Football API",
                                    Duration.ofSeconds(seconds)
                            );
                        }
                        throw new IntegrationException("Client error calling Football API: " + httpResponse.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, httpResponse) -> {
                        throw new ExternalServiceUnavailableException(
                                "Football API service unavailable: " + httpResponse.getStatusCode()
                        );
                    })
                    .body(MatchesResponseDto.class);

            if (response == null || response.matches() == null) {
                return Collections.emptyList();
            }

            return response.matches().stream()
                    .map(matchMapper::toDomain)
                    .toList();

        } catch (ResourceAccessException e) {
            throw new ExternalTimeoutException("Timeout or network error connecting to Football API", e);
        }
    }
}