package com.tdp.ms.template.settings.application.retrieve;

import com.tdp.ms.template.settings.domain.GeneralSetting;
import com.tdp.ms.template.settings.domain.SettingKeyCatalog;
import com.tdp.ms.template.settings.domain.SettingsGateway;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SettingsAppRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsAppRetriever.class);
    private static final String OPERATION = "template.settings-app.retrieve";

    private final SettingsGateway gateway;

    public SettingsAppRetriever(SettingsGateway gateway) {
        this.gateway = gateway;
    }

    public Mono<RetrieveSettingsAppResponse> retrieve(String correlationId) {
        return gateway.findByKeys(SettingKeyCatalog.ALL_KEYS)
                .collectList()
                .map(this::toResponse)
                .doOnSuccess(response -> LOG.info(
                        "operation={} correlationId={} aggregateId=settings-app entityId=settings-app idempotencyKey=unavailable outcome=completed",
                        OPERATION, correlationId))
                .doOnError(error -> LOG.warn(
                        "operation={} correlationId={} aggregateId=settings-app entityId=settings-app idempotencyKey=unavailable outcome=rejected cause={}",
                        OPERATION, correlationId, error.getClass().getSimpleName()));
    }

    private RetrieveSettingsAppResponse toResponse(List<GeneralSetting> settings) {
        Map<String, String> primarySettings = new LinkedHashMap<>();
        List<AdditionalSettingResponse> additionalData = new ArrayList<>();

        for (GeneralSetting setting : settings) {
            String formattedKey = formatKey(setting.key());
            if (SettingKeyCatalog.ADDITIONAL_KEYS.contains(setting.key())) {
                additionalData.add(new AdditionalSettingResponse(formattedKey, setting.value()));
            } else {
                primarySettings.put(formattedKey, setting.value());
            }
        }

        return new RetrieveSettingsAppResponse(primarySettings, List.copyOf(additionalData));
    }

    private static String formatKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace("_", "");
    }
}
