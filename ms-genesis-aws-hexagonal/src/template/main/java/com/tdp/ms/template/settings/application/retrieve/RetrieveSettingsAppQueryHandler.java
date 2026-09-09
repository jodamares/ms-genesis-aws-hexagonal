package com.tdp.ms.template.settings.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RetrieveSettingsAppQueryHandler implements QueryHandler<RetrieveSettingsAppQuery, RetrieveSettingsAppResponse> {
    private final SettingsAppRetriever retriever;

    public RetrieveSettingsAppQueryHandler(SettingsAppRetriever retriever) {
        this.retriever = retriever;
    }

    @Override
    public Mono<RetrieveSettingsAppResponse> handle(RetrieveSettingsAppQuery query) {
        return retriever.retrieve(query.correlationId());
    }
}
