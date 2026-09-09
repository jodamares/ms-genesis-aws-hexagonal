package com.tdp.ms.shared.domain.bus.query;

import reactor.core.publisher.Mono;

public interface QueryHandler<Q extends Query, R extends Response> {
    Mono<R> handle(Q query);
}
