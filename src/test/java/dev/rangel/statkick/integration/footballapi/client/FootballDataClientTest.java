package dev.rangel.statkick.integration.footballapi.client;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.integration.footballapi.exception.ExternalServiceUnavailableException;
import dev.rangel.statkick.integration.footballapi.exception.ExternalTimeoutException;
import dev.rangel.statkick.integration.footballapi.exception.RateLimitExceededException;
import dev.rangel.statkick.integration.footballapi.mapper.MatchMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest; // IMPORT CORRETO!
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
class FootballDataClientTest {

    private static final String BASE_URL = "https://api.football-data.org/v4";
    private static final String TOKEN = "dummy-test-token";

    @Autowired
    private RestClient.Builder restClientBuilder;

    private FootballDataClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        RestClient restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("X-Auth-Token", TOKEN)
                .build();

        client = new FootballDataClient(restClient, new MatchMapper());
    }

    @Test
    @DisplayName("Should successfully fetch and map matches on 200 OK")
    void shouldFetchMatchesSuccessfullyOn200() {
        String jsonPayload = """
            {
              "matches": [
                {
                  "id": 1001,
                  "utcDate": "2026-08-21T19:00:00Z",
                  "status": "FINISHED",
                  "matchday": 1,
                  "homeTeam": { "id": 57, "name": "Arsenal FC" },
                  "awayTeam": { "id": 1076, "name": "Coventry City FC" },
                  "score": { "fullTime": { "home": 3, "away": 0 } }
                }
              ]
            }
            """;

        mockServer.expect(requestTo(BASE_URL + "/competitions/PL/matches"))
                .andExpect(header("X-Auth-Token", TOKEN))
                .andRespond(withSuccess(jsonPayload, MediaType.APPLICATION_JSON));

        List<Match> matches = client.fetchMatches("PL");

        assertNotNull(matches);
        assertEquals(1, matches.size());
        assertEquals(1001L, matches.get(0).getId());
        assertEquals(MatchStatus.FINISHED, matches.get(0).getStatus());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException with correct Duration on 429 Too Many Requests")
    void shouldThrowRateLimitExceededOn429() {
        mockServer.expect(requestTo(BASE_URL + "/competitions/PL/matches"))
                .andExpect(header("X-Auth-Token", TOKEN))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "30"));

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> client.fetchMatches("PL")
        );

        assertEquals("Rate limit exceeded for Football API", exception.getMessage());
        assertEquals(Duration.ofSeconds(30), exception.getRetryAfter());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ExternalServiceUnavailableException on 503 Service Unavailable")
    void shouldThrowExternalServiceUnavailableOn503() {
        mockServer.expect(requestTo(BASE_URL + "/competitions/PL/matches"))
                .andExpect(header("X-Auth-Token", TOKEN))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        ExternalServiceUnavailableException exception = assertThrows(
                ExternalServiceUnavailableException.class,
                () -> client.fetchMatches("PL")
        );

        assertTrue(exception.getMessage().contains("Football API service unavailable"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ExternalTimeoutException on network I/O timeout")
    void shouldThrowExternalTimeoutExceptionOnNetworkTimeout() {
        mockServer.expect(requestTo(BASE_URL + "/competitions/PL/matches"))
                .andExpect(header("X-Auth-Token", TOKEN))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out");
                });

        ExternalTimeoutException exception = assertThrows(
                ExternalTimeoutException.class,
                () -> client.fetchMatches("PL")
        );

        assertEquals("Timeout or network error connecting to Football API", exception.getMessage());

        mockServer.verify();
    }
}