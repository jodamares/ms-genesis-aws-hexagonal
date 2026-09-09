package com.tdp.ms.apps.backend.http;

import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;

@Component
public class HttpCachePolicy {
    private final HttpCacheProperties properties;

    public HttpCachePolicy(HttpCacheProperties properties) {
        this.properties = properties;
    }

    public CacheControl keyList() {
        return cacheable(properties.keyListMaxAge());
    }

    public CacheControl settingsApp() {
        return cacheable(properties.settingsAppMaxAge());
    }

    public CacheControl widgets() {
        return cacheable(properties.widgetsMaxAge());
    }

    private CacheControl cacheable(Duration maxAge) {
        if (!properties.enabled() || maxAge == null || maxAge.isZero() || maxAge.isNegative()) {
            return CacheControl.noStore();
        }
        return CacheControl.maxAge(maxAge)
                .sMaxAge(maxAge)
                .cachePublic();
    }
}
