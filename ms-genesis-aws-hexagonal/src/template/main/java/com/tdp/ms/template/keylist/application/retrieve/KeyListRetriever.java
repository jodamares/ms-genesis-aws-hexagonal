package com.tdp.ms.template.keylist.application.retrieve;

import com.tdp.ms.shared.domain.error.InvalidInputException;
import com.tdp.ms.shared.domain.error.ProcessingException;
import com.tdp.ms.template.keylist.domain.KeyList;
import com.tdp.ms.template.keylist.domain.KeyListDetail;
import com.tdp.ms.template.keylist.domain.KeyListGateway;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KeyListRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(KeyListRetriever.class);
    private static final String OPERATION = "template.key-list.retrieve";

    private final KeyListGateway gateway;

    public KeyListRetriever(KeyListGateway gateway) {
        this.gateway = gateway;
    }

    public Mono<RetrieveKeyListResponse> retrieve(String key, String correlationId) {
        if (key == null || key.isBlank()) {
            return Mono.error(new InvalidInputException("TEMPLATE_KEY_REQUIRED", "key is required"));
        }
        return gateway.findByDescription(key)
                .switchIfEmpty(Mono.error(new ProcessingException("TEMPLATE_KEY_LIST_NOT_AVAILABLE", "Key list is not available")))
                .map(this::toResponse)
                .doOnSuccess(response -> LOG.info(
                        "operation={} correlationId={} aggregateId={} entityId={} idempotencyKey=unavailable outcome=completed",
                        OPERATION, correlationId, key, key))
                .doOnError(error -> LOG.warn(
                        "operation={} correlationId={} aggregateId={} entityId={} idempotencyKey=unavailable outcome=rejected cause={}",
                        OPERATION, correlationId, key, key, error.getClass().getSimpleName()));
    }

    private RetrieveKeyListResponse toResponse(KeyList keyList) {
        Set<KeyListDetailResponse> details = keyList.details().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RetrieveKeyListResponse(keyList.description(), details);
    }

    private KeyListDetailResponse toDetailResponse(KeyListDetail detail) {
        return new KeyListDetailResponse(detail.key(), detail.value(), detail.dataType());
    }
}
