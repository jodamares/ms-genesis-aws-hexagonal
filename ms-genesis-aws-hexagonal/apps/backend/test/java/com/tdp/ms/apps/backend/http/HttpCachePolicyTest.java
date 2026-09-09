package com.tdp.ms.apps.backend.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class HttpCachePolicyTest {
    @Test
    void shouldBuildPublicSharedCacheHeader() {
        HttpCachePolicy policy = new HttpCachePolicy(new HttpCacheProperties(
                true,
                Duration.ofSeconds(300),
                Duration.ofSeconds(180),
                Duration.ofSeconds(120)));

        String header = policy.keyList().getHeaderValue();

        assertTrue(header.contains("max-age=300"));
        assertTrue(header.contains("public"));
        assertTrue(header.contains("s-maxage=300"));
    }

    @Test
    void shouldBuildNoStoreHeaderWhenDisabled() {
        HttpCachePolicy policy = new HttpCachePolicy(new HttpCacheProperties(
                false,
                Duration.ofSeconds(300),
                Duration.ofSeconds(180),
                Duration.ofSeconds(120)));

        assertEquals("no-store", policy.widgets().getHeaderValue());
    }

    @Test
    void shouldBuildNoStoreHeaderWhenMaxAgeIsZero() {
        HttpCachePolicy policy = new HttpCachePolicy(new HttpCacheProperties(
                true,
                Duration.ZERO,
                Duration.ofSeconds(180),
                Duration.ofSeconds(120)));

        assertEquals("no-store", policy.keyList().getHeaderValue());
    }
}
