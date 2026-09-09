package com.tdp.ms.apps.backend.controller.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tdp.ms.apps.backend.http.HttpCachePolicy;
import com.tdp.ms.shared.domain.bus.query.QueryBus;
import com.tdp.ms.shared.infrastructure.observability.CorrelationContextFilter;
import com.tdp.ms.template.settings.application.retrieve.AdditionalSettingResponse;
import com.tdp.ms.template.settings.application.retrieve.RetrieveSettingsAppQuery;
import com.tdp.ms.template.settings.application.retrieve.RetrieveSettingsAppResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/templateorganization/v1")
public class TemplateSettingsAppGetController {
    private final QueryBus queryBus;
    private final ObjectMapper objectMapper;
    private final HttpCachePolicy cachePolicy;

    public TemplateSettingsAppGetController(QueryBus queryBus, ObjectMapper objectMapper, HttpCachePolicy cachePolicy) {
        this.queryBus = queryBus;
        this.objectMapper = objectMapper;
        this.cachePolicy = cachePolicy;
    }

    @GetMapping(value = "/settingsapp", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ObjectNode>> retrieveSettingsApp() {
        return Mono.deferContextual(context -> {
            String correlationId = context.getOrDefault(CorrelationContextFilter.CORRELATION_ID, "unknown");
            return queryBus.<RetrieveSettingsAppResponse>ask(new RetrieveSettingsAppQuery(correlationId))
                    .map(this::toJson)
                    .map(response -> ResponseEntity.ok()
                            .cacheControl(cachePolicy.settingsApp())
                            .body(response));
        });
    }

    private ObjectNode toJson(RetrieveSettingsAppResponse response) {
        ObjectNode root = objectMapper.createObjectNode();
        response.settings().forEach(root::put);
        root.set("additionalData", objectMapper.valueToTree(response.additionalData().stream()
                .map(this::toAdditionalData)
                .toList()));
        return root;
    }

    private AdditionalData toAdditionalData(AdditionalSettingResponse response) {
        return new AdditionalData(response.key(), response.value());
    }

    private record AdditionalData(String key, String value) {
    }
}
