package com.tdp.ms.apps.backend.controller.template;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tdp.ms.apps.backend.http.HttpCacheProperties;
import com.tdp.ms.apps.backend.http.HttpCachePolicy;
import com.tdp.ms.shared.domain.bus.query.QueryBus;
import com.tdp.ms.shared.infrastructure.observability.CorrelationContextFilter;
import com.tdp.ms.template.keylist.application.retrieve.RetrieveKeyListQuery;
import com.tdp.ms.template.keylist.application.retrieve.RetrieveKeyListResponse;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class TemplateKeyListGetControllerTest {
    @Test
    void shouldReturnCacheHeader() {
        QueryBus queryBus = mock(QueryBus.class);
        RetrieveKeyListResponse response = new RetrieveKeyListResponse("template", Set.of());
        when(queryBus.<RetrieveKeyListResponse>ask(any(RetrieveKeyListQuery.class))).thenReturn(Mono.just(response));

        TemplateKeyListGetController controller = new TemplateKeyListGetController(queryBus, cachePolicy());

        StepVerifier.create(controller.retrieveKeyList("template")
                        .contextWrite(Context.of(CorrelationContextFilter.CORRELATION_ID, "corr-1")))
                .assertNext(entity -> {
                    assertSame(response, entity.getBody());
                    assertCacheHeader(entity.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
                })
                .verifyComplete();

        verify(queryBus).ask(new RetrieveKeyListQuery("template", "corr-1"));
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
