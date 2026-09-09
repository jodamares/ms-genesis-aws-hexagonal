package com.tdp.ms.apps.backend.controller.template;

import com.tdp.ms.apps.backend.http.HttpCachePolicy;
import com.tdp.ms.shared.domain.bus.query.QueryBus;
import com.tdp.ms.shared.infrastructure.observability.CorrelationContextFilter;
import com.tdp.ms.template.keylist.application.retrieve.RetrieveKeyListQuery;
import com.tdp.ms.template.keylist.application.retrieve.RetrieveKeyListResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/templateorganization/v1")
public class TemplateKeyListGetController {
    private final QueryBus queryBus;
    private final HttpCachePolicy cachePolicy;

    public TemplateKeyListGetController(QueryBus queryBus, HttpCachePolicy cachePolicy) {
        this.queryBus = queryBus;
        this.cachePolicy = cachePolicy;
    }

    @GetMapping(value = "/keylist", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RetrieveKeyListResponse>> retrieveKeyList(@RequestParam("key") @NotBlank String key) {
        return Mono.deferContextual(context -> {
            String correlationId = context.getOrDefault(CorrelationContextFilter.CORRELATION_ID, "unknown");
            return queryBus.<RetrieveKeyListResponse>ask(new RetrieveKeyListQuery(key, correlationId))
                    .map(response -> ResponseEntity.ok()
                            .cacheControl(cachePolicy.keyList())
                            .body(response));
        });
    }
}
