package com.tdp.ms.apps.backend.http;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "template.http-cache")
public record HttpCacheProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("300s") Duration keyListMaxAge,
        @DefaultValue("300s") Duration settingsAppMaxAge,
        @DefaultValue("300s") Duration widgetsMaxAge) {
}
