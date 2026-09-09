package com.tdp.ms.shared.infrastructure.observability;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order(0)
public class CorrelationContextFilter implements WebFilter {
    public static final String CORRELATION_ID = "correlationId";
    public static final String REQUEST_ID = "requestId";
    public static final String IDEMPOTENCY_KEY = "idempotencyKey";

    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String HEADER_UNICA_PID = "UNICA-PID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = headerOrNew(request, HEADER_CORRELATION_ID, HEADER_UNICA_PID);
        String requestId = headerOrNew(request, HEADER_REQUEST_ID);
        String idempotencyKey = request.getHeaders().getFirst(HEADER_IDEMPOTENCY_KEY);

        exchange.getResponse().getHeaders().set(HEADER_CORRELATION_ID, correlationId);
        exchange.getResponse().getHeaders().set(HEADER_REQUEST_ID, requestId);

        Context context = Context.of(CORRELATION_ID, correlationId, REQUEST_ID, requestId);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            context = context.put(IDEMPOTENCY_KEY, idempotencyKey);
        }

        Context reactorContext = context;
        return chain.filter(exchange)
                .doOnSubscribe(subscription -> putMdc(correlationId, requestId, idempotencyKey))
                .doFinally(signalType -> clearMdc())
                .contextWrite(reactorContext);
    }

    private static void putMdc(String correlationId, String requestId, String idempotencyKey) {
        MDC.put(CORRELATION_ID, correlationId);
        MDC.put(REQUEST_ID, requestId);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            MDC.put(IDEMPOTENCY_KEY, idempotencyKey);
        }
    }

    private static void clearMdc() {
        MDC.remove(CORRELATION_ID);
        MDC.remove(REQUEST_ID);
        MDC.remove(IDEMPOTENCY_KEY);
    }

    private static String headerOrNew(ServerHttpRequest request, String... headers) {
        for (String header : headers) {
            String value = request.getHeaders().getFirst(header);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return UUID.randomUUID().toString();
    }
}
