package com.tdp.ms.shared.application.idempotency;

public record IdempotencyOutcome<T>(T value, boolean replayed) {
    public static <T> IdempotencyOutcome<T> fresh(T value) {
        return new IdempotencyOutcome<>(value, false);
    }

    public static <T> IdempotencyOutcome<T> replayed(T value) {
        return new IdempotencyOutcome<>(value, true);
    }
}
