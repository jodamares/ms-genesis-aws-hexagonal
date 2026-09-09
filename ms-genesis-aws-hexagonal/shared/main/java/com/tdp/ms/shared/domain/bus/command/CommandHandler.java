package com.tdp.ms.shared.domain.bus.command;

import reactor.core.publisher.Mono;

public interface CommandHandler<C extends Command> {
    Mono<Void> handle(C command);
}
