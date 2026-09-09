package com.tdp.ms.shared.domain.bus.query;

import reactor.core.publisher.Mono;

public interface QueryBus {
    <R extends Response> Mono<R> ask(Query query);
}
