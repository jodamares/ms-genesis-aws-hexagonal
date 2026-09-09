package com.tdp.ms.template.keylist.domain;

import reactor.core.publisher.Mono;

public interface KeyListGateway {
    Mono<KeyList> findByDescription(String description);
}
