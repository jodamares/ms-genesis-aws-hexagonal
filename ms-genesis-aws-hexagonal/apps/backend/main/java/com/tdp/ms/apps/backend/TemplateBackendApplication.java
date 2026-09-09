package com.tdp.ms.apps.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication(scanBasePackages = "com.tdp.ms")
@ConfigurationPropertiesScan(basePackages = "com.tdp.ms")
public class TemplateBackendApplication {
    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(TemplateBackendApplication.class, args);
    }
}
