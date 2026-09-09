package com.tdp.ms.shared.application.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdp.ms.shared.domain.idempotency.IdempotencyStore;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnBean(IdempotencyStore.class)
public class IdempotentExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(IdempotentExecutor.class);

    private final IdempotencyStore store;
    private final ObjectMapper objectMapper;

    public IdempotentExecutor(IdempotencyStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    public <T> Mono<IdempotencyOutcome<T>> execute(
            String operation,
            String correlationId,
            String aggregateId,
            String idempotencyKey,
            Duration ttl,
            Class<T> responseType,
            Mono<T> command) {
        return store.reserve(idempotencyKey, ttl)
                .flatMap(reserved -> reserved
                        ? runAndStore(operation, correlationId, aggregateId, idempotencyKey, ttl, command)
                        : replay(operation, correlationId, aggregateId, idempotencyKey, responseType));
    }

    private <T> Mono<IdempotencyOutcome<T>> runAndStore(
            String operation,
            String correlationId,
            String aggregateId,
            String idempotencyKey,
            Duration ttl,
            Mono<T> command) {
        return command
                .flatMap(result -> persist(operation, correlationId, aggregateId, idempotencyKey, ttl, result))
                .onErrorResume(error -> store.release(idempotencyKey)
                        .doOnSuccess(ignored -> LOG.warn(
                                "operation={} correlationId={} aggregateId={} entityId=unavailable idempotencyKey={} outcome=released_after_failure cause={}",
                                operation, correlationId, aggregateId, idempotencyKey, error.getClass().getSimpleName()))
                        .then(Mono.error(error)));
    }

    private <T> Mono<IdempotencyOutcome<T>> persist(
            String operation,
            String correlationId,
            String aggregateId,
            String idempotencyKey,
            Duration ttl,
            T result) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException error) {
            LOG.error(
                    "operation={} correlationId={} aggregateId={} entityId=unavailable idempotencyKey={} outcome=serialize_rejected cause={}",
                    operation, correlationId, aggregateId, idempotencyKey, error.getClass().getSimpleName(), error);
            return Mono.error(error);
        }

        return store.store(idempotencyKey, payload, ttl)
                .doOnSuccess(ignored -> LOG.info(
                        "operation={} correlationId={} aggregateId={} entityId=unavailable idempotencyKey={} outcome=stored ttlSeconds={}",
                        operation, correlationId, aggregateId, idempotencyKey, ttl.toSeconds()))
                .thenReturn(IdempotencyOutcome.fresh(result));
    }

    private <T> Mono<IdempotencyOutcome<T>> replay(
            String operation,
            String correlationId,
            String aggregateId,
            String idempotencyKey,
            Class<T> responseType) {
        return store.find(idempotencyKey)
                .switchIfEmpty(Mono.error(new IdempotencyConflictException("Idempotency key is reserved")))
                .flatMap(payload -> deserialize(operation, correlationId, aggregateId, idempotencyKey, payload, responseType))
                .doOnNext(outcome -> LOG.info(
                        "operation={} correlationId={} aggregateId={} entityId=unavailable idempotencyKey={} outcome=replayed",
                        operation, correlationId, aggregateId, idempotencyKey));
    }

    private <T> Mono<IdempotencyOutcome<T>> deserialize(
            String operation,
            String correlationId,
            String aggregateId,
            String idempotencyKey,
            String payload,
            Class<T> responseType) {
        try {
            return Mono.just(IdempotencyOutcome.replayed(objectMapper.readValue(payload, responseType)));
        } catch (JsonProcessingException error) {
            LOG.error(
                    "operation={} correlationId={} aggregateId={} entityId=unavailable idempotencyKey={} outcome=deserialize_rejected cause={}",
                    operation, correlationId, aggregateId, idempotencyKey, error.getClass().getSimpleName(), error);
            return Mono.error(error);
        }
    }

    public static class IdempotencyConflictException extends RuntimeException {
        public IdempotencyConflictException(String message) {
            super(message);
        }
    }
}
