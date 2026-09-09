package com.tdp.ms.shared.application.idempotency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tdp.ms.shared.domain.idempotency.IdempotencyStore;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class IdempotentExecutorTest {
    @Mock
    IdempotencyStore store;

    @Test
    void shouldExecuteAndStoreFreshOutcome() {
        Duration ttl = Duration.ofMinutes(5);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        IdempotentExecutor executor = new IdempotentExecutor(store, mapper);
        when(store.reserve("key-1", ttl)).thenReturn(Mono.just(true));
        when(store.store("key-1", "{\"value\":\"ok\"}", ttl)).thenReturn(Mono.empty());

        StepVerifier.create(executor.execute(
                        "template.command.create",
                        "corr-1",
                        "agg-1",
                        "key-1",
                        ttl,
                        SampleResponse.class,
                        Mono.just(new SampleResponse("ok"))))
                .assertNext(outcome -> {
                    assertFalse(outcome.replayed());
                    assertTrue(outcome.value().value().equals("ok"));
                })
                .verifyComplete();

        verify(store).store("key-1", "{\"value\":\"ok\"}", ttl);
    }

    @Test
    void shouldReplayCompletedOutcome() {
        Duration ttl = Duration.ofMinutes(5);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        IdempotentExecutor executor = new IdempotentExecutor(store, mapper);
        when(store.reserve("key-1", ttl)).thenReturn(Mono.just(false));
        when(store.find("key-1")).thenReturn(Mono.just("{\"value\":\"ok\"}"));

        StepVerifier.create(executor.execute(
                        "template.command.create",
                        "corr-1",
                        "agg-1",
                        "key-1",
                        ttl,
                        SampleResponse.class,
                        Mono.just(new SampleResponse("ignored"))))
                .assertNext(outcome -> {
                    assertTrue(outcome.replayed());
                    assertTrue(outcome.value().value().equals("ok"));
                })
                .verifyComplete();
    }

    record SampleResponse(String value) {
    }
}
