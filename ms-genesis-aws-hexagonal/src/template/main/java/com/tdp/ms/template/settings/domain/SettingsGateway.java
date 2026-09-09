package com.tdp.ms.template.settings.domain;

import java.util.Collection;
import reactor.core.publisher.Flux;

public interface SettingsGateway {
    Flux<GeneralSetting> findByKeys(Collection<String> keys);
}
