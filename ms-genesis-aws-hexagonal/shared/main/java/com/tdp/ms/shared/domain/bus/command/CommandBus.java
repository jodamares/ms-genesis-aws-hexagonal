package com.tdp.ms.shared.domain.bus.command;

import reactor.core.publisher.Mono;

public interface CommandBus {
    Mono<Void> dispatch(Command command);
}
