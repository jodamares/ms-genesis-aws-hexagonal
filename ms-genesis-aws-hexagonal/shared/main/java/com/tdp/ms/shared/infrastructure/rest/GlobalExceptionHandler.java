package com.tdp.ms.shared.infrastructure.rest;

import com.tdp.ms.shared.application.idempotency.IdempotentExecutor;
import com.tdp.ms.shared.domain.error.ConflictException;
import com.tdp.ms.shared.domain.error.DomainException;
import com.tdp.ms.shared.domain.error.InvalidInputException;
import com.tdp.ms.shared.domain.error.NotFoundException;
import com.tdp.ms.shared.infrastructure.observability.CorrelationContextFilter;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.template.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidInputException.class)
    public Mono<ResponseEntity<ApiError>> handleInvalidInput(InvalidInputException exception) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, exception);
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ApiError>> handleConflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ApiError>> handleDomain(DomainException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(IdempotentExecutor.IdempotencyConflictException.class)
    public Mono<ResponseEntity<ApiError>> handleIdempotencyConflict(IdempotentExecutor.IdempotencyConflictException exception) {
        return correlationId().map(correlationId -> {
            LOG.warn("operation=idempotency.execute correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=conflict cause={}",
                    correlationId, exception.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("Retry-After", "1")
                    .body(ApiError.of("TEMPLATE_IDEMPOTENCY_CONFLICT", exception.getMessage(), correlationId));
        });
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ApiError>> handleBadRequest(ServerWebInputException exception) {
        return correlationId().map(correlationId -> {
            LOG.warn("operation=http.decode correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=bad_request cause={}",
                    correlationId, exception.getReason());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiError.of("TEMPLATE_BAD_REQUEST", exception.getReason(), correlationId));
        });
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ApiError>> handleConstraintViolation(ConstraintViolationException exception) {
        return correlationId().map(correlationId -> {
            LOG.warn("operation=http.validate correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=rejected cause={}",
                    correlationId, exception.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiError.of("TEMPLATE_BAD_REQUEST", "Invalid request parameters", correlationId));
        });
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleWebExchangeBind(WebExchangeBindException exception) {
        return correlationId().map(correlationId -> {
            LOG.warn("operation=http.validate correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=rejected cause={}",
                    correlationId, exception.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiError.of("TEMPLATE_BAD_REQUEST", "Invalid request body", correlationId));
        });
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ApiError>> handleUnexpected(Throwable exception) {
        return correlationId().map(correlationId -> {
            LOG.error("operation=http.request correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=unexpected cause={}",
                    correlationId, exception.getClass().getSimpleName(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiError.of("TEMPLATE_INTERNAL_ERROR", "Unexpected error processing request", correlationId));
        });
    }

    private static Mono<ResponseEntity<ApiError>> error(HttpStatus status, DomainException exception) {
        return correlationId().map(correlationId -> {
            LOG.warn("operation=domain.validate correlationId={} aggregateId=unavailable entityId=unavailable idempotencyKey=unavailable outcome=rejected code={} cause={}",
                    correlationId, exception.getCode(), exception.getMessage());
            return ResponseEntity.status(status).body(ApiError.of(exception.getCode(), exception.getMessage(), correlationId));
        });
    }

    private static Mono<String> correlationId() {
        return Mono.deferContextual(context -> Mono.just(context.getOrDefault(CorrelationContextFilter.CORRELATION_ID, "unknown")));
    }
}
