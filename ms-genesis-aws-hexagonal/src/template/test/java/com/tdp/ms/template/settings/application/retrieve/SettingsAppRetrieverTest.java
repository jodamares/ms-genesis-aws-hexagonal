package com.tdp.ms.template.settings.application.retrieve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.tdp.ms.template.settings.domain.GeneralSetting;
import com.tdp.ms.template.settings.domain.SettingsGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SettingsAppRetrieverTest {
    @Mock
    SettingsGateway gateway;

    @Test
    void shouldRetrieveSettingsWithAdditionalData() {
        when(gateway.findByKeys(anyCollection())).thenReturn(Flux.just(
                new GeneralSetting(1, "ANDROID_APP_VERSION", "12.0.50"),
                new GeneralSetting(2, "BUY_MPLAY", "false")));

        SettingsAppRetriever retriever = new SettingsAppRetriever(gateway);

        StepVerifier.create(retriever.retrieve("corr-1"))
                .assertNext(response -> {
                    assertEquals("12.0.50", response.settings().get("androidappversion"));
                    assertEquals(1, response.additionalData().size());
                    assertEquals("buymplay", response.additionalData().get(0).key());
                })
                .verifyComplete();
    }
}
