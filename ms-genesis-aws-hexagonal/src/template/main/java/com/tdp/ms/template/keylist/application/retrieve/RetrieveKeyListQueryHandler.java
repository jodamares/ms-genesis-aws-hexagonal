package com.tdp.ms.template.keylist.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RetrieveKeyListQueryHandler implements QueryHandler<RetrieveKeyListQuery, RetrieveKeyListResponse> {
    private final KeyListRetriever retriever;

    public RetrieveKeyListQueryHandler(KeyListRetriever retriever) {
        this.retriever = retriever;
    }

    @Override
    public Mono<RetrieveKeyListResponse> handle(RetrieveKeyListQuery query) {
        return retriever.retrieve(query.key(), query.correlationId());
    }
}
