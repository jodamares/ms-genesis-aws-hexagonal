package com.tdp.ms.template.widget.application.search;

import com.tdp.ms.shared.domain.bus.query.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SearchWidgetsQueryHandler implements QueryHandler<SearchWidgetsQuery, SearchWidgetsResponse> {
    private final WidgetSearcher searcher;

    public SearchWidgetsQueryHandler(WidgetSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public Mono<SearchWidgetsResponse> handle(SearchWidgetsQuery query) {
        return searcher.search(query.productId(), query.productPs(), query.correlationId());
    }
}
