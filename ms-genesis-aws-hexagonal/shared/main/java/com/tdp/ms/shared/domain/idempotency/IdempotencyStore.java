package com.tdp.ms.shared.domain.idempotency;

import java.time.Duration;
import reactor.core.publisher.Mono;

public interface IdempotencyStore {
    Mono<Boolean> reserve(String key, Duration ttl);

    Mono<Void> store(String key, String serializedOutcome, Duration ttl);

    Mono<String> find(String key);

    Mono<Void> release(String key);
}
