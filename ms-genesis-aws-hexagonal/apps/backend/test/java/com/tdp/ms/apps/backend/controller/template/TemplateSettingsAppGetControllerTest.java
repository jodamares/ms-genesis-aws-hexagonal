package com.tdp.ms.apps.backend.controller.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdp.ms.apps.backend.http.HttpCacheProperties;
import com.tdp.ms.apps.backend.http.HttpCachePolicy;
import com.tdp.ms.shared.domain.bus.query.QueryBus;
import com.tdp.ms.shared.infrastructure.observability.CorrelationContextFilter;
import com.tdp.ms.template.settings.application.retrieve.RetrieveSettingsAppQuery;
import com.tdp.ms.template.settings.application.retrieve.RetrieveSettingsAppResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class TemplateSettingsAppGetControllerTest {
    @Test
    void shouldReturnCacheHeader() {
        QueryBus queryBus = mock(QueryBus.class);
        RetrieveSettingsAppResponse response = new RetrieveSettingsAppResponse(Map.of("feature", "enabled"), List.of());
        when(queryBus.<RetrieveSettingsAppResponse>ask(any(RetrieveSettingsAppQuery.class))).thenReturn(Mono.just(response));

        TemplateSettingsAppGetController controller = new TemplateSettingsAppGetController(
                queryBus,
                new ObjectMapper(),
                cachePolicy());

        StepVerifier.create(controller.retrieveSettingsApp()
                        .contextWrite(Context.of(CorrelationContextFilter.CORRELATION_ID, "corr-1")))
                .assertNext(entity -> {
                    assertEquals("enabled", entity.getBody().get("feature").asText());
                    assertCacheHeader(entity.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
                })
                .verifyComplete();

        verify(queryBus).ask(new RetrieveSettingsAppQuery("corr-1"));
    }

    private static HttpCachePolicy cachePolicy() {
        return new HttpCachePolicy(new HttpCacheProperties(
                true,
                Duration.ofSeconds(300),
                Duration.ofSeconds(300),
                Duration.ofSeconds(300)));
    }

    private static void assertCacheHeader(String header) {
        assertNotNull(header);
        assertTrue(header.contains("max-age=300"));
        assertTrue(header.contains("public"));
        assertTrue(header.contains("s-maxage=300"));
    }
}
