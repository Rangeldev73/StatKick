package dev.rangel.statkick.integration.footballapi.cache;

import com.github.benmanes.caffeine.cache.Ticker;
import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.port.FootballMatchPort;
import dev.rangel.statkick.infrastructure.config.CacheConfig;
import dev.rangel.statkick.integration.footballapi.exception.ExternalServiceUnavailableException;
import dev.rangel.statkick.integration.footballapi.exception.ExternalTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        CacheConfig.class,
        CachedFootballMatchAdapter.class,
        CachedFootballMatchAdapterTest.FakeTickerConfig.class
})
@TestPropertySource(properties = "statkick.cache.matches-ttl=20m")
class CachedFootballMatchAdapterTest {

    @Autowired
    private FootballMatchPort cachedAdapter;

    @MockitoBean(name = "resilientFootballMatchAdapter")
    private FootballMatchPort mockClient;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private FakeTicker fakeTicker;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("matchesByCompetition").clear();
        fakeTicker.reset();
        reset(mockClient);
    }

    @Test
    @DisplayName("Should serve from cache on second call within TTL")
    void shouldServeFromCacheWithinTTL() {
        when(mockClient.fetchMatches("PL")).thenReturn(Collections.emptyList());

        cachedAdapter.fetchMatches("PL");
        cachedAdapter.fetchMatches("PL");

        verify(mockClient, times(1)).fetchMatches("PL");
    }

    @Test
    @DisplayName("Should fetch from upstream again after TTL expires")
    void shouldFetchAgainAfterTTL() {
        when(mockClient.fetchMatches("PL")).thenReturn(Collections.emptyList());

        cachedAdapter.fetchMatches("PL");
        fakeTicker.advanceTime(Duration.ofMinutes(21));
        cachedAdapter.fetchMatches("PL");

        verify(mockClient, times(2)).fetchMatches("PL");
    }

    @Test
    @DisplayName("Should single-flight N concurrent requests on cold cache")
    void shouldSingleFlightConcurrentRequests() throws InterruptedException, ExecutionException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyGate = new CountDownLatch(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        when(mockClient.fetchMatches("PL")).thenAnswer(invocation -> {
            Thread.sleep(100);
            return Collections.emptyList();
        });

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();
                    return cachedAdapter.fetchMatches("PL");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                } finally {
                    endGate.countDown();
                }
            }));
        }

        readyGate.await();
        startGate.countDown();
        boolean completedInTime = endGate.await(3, TimeUnit.SECONDS);
        assertTrue(completedInTime, "All threads should complete within timeout");

        for (Future<?> future : futures) {
            future.get();
        }

        verify(mockClient, times(1)).fetchMatches("PL");
        executor.shutdown();
    }

    @Test
    @DisplayName("Measure calls when upstream always throws ExternalTimeoutException with 10 threads")
    void measureUpstreamCallsOnFailureWithConcurrentThreads() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyGate = new CountDownLatch(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        when(mockClient.fetchMatches("PL")).thenAnswer(invocation -> {
            Thread.sleep(200);
            throw new ExternalTimeoutException("Timeout", new RuntimeException());
        });

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyGate.countDown();
                try {
                    startGate.await();
                    cachedAdapter.fetchMatches("PL");
                } catch (Exception ignored) {
                } finally {
                    endGate.countDown();
                }
            }));
        }

        readyGate.await();
        startGate.countDown();
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        assertTrue(completed);

        int actualCalls = mockingDetails(mockClient).getInvocations().size();
        System.out.println("TOTAL CALLS MEASURED ON FAILURE: " + actualCalls);

        executor.shutdown();
    }

    @Test
    @DisplayName("Should not cache exceptions and allow subsequent retries")
    void shouldNotCacheExceptions() {
        when(mockClient.fetchMatches("PL"))
                .thenThrow(new ExternalServiceUnavailableException("API Down"))
                .thenReturn(Collections.emptyList());

        assertThrows(ExternalServiceUnavailableException.class, () -> cachedAdapter.fetchMatches("PL"));

        List<Match> matches = cachedAdapter.fetchMatches("PL");
        assertNotNull(matches);
        verify(mockClient, times(2)).fetchMatches("PL");
    }

    @TestConfiguration
    static class FakeTickerConfig {
        @Bean
        @Primary
        public FakeTicker testTicker() {
            return new FakeTicker();
        }
    }

    static class FakeTicker implements Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        public void advanceTime(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }

        public void reset() {
            nanos.set(0);
        }
    }
}