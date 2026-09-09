package com.tdp.ms.template.widget.domain;

import java.util.Collection;
import reactor.core.publisher.Flux;

public interface WidgetGateway {
    Flux<ProductWidget> findProductWidgets(Integer productId);

    Flux<WidgetDefinition> findWidgets(Collection<Integer> widgetIds);

    Flux<WidgetElement> findElements(Collection<Integer> widgetIds);

    Flux<ProductLine> findProductLines(String psCode);
}
