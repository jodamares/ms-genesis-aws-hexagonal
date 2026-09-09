package com.tdp.ms.template.keylist.application.retrieve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.tdp.ms.shared.domain.error.ProcessingException;
import com.tdp.ms.template.keylist.domain.KeyList;
import com.tdp.ms.template.keylist.domain.KeyListDetail;
import com.tdp.ms.template.keylist.domain.KeyListGateway;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class KeyListRetrieverTest {
    @Mock
    KeyListGateway gateway;

    @Test
    void shouldRetrieveKeyList() {
        Set<KeyListDetail> details = new LinkedHashSet<>();
        details.add(new KeyListDetail(1, "upsell", "{\"order\":\"D\"}", "json"));
        details.add(new KeyListDetail(2, "downsell", "{\"order\":\"A\"}", "json"));
        when(gateway.findByDescription("CAPL_Mobile")).thenReturn(Mono.just(new KeyList(1, "CAPL_Mobile", details)));

        KeyListRetriever retriever = new KeyListRetriever(gateway);

        StepVerifier.create(retriever.retrieve("CAPL_Mobile", "corr-1"))
                .assertNext(response -> {
                    assertEquals("CAPL_Mobile", response.key());
                    assertEquals(2, response.list().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenKeyListIsMissing() {
        when(gateway.findByDescription("missing")).thenReturn(Mono.empty());

        KeyListRetriever retriever = new KeyListRetriever(gateway);

        StepVerifier.create(retriever.retrieve("missing", "corr-1"))
                .expectError(ProcessingException.class)
                .verify();
    }
}
