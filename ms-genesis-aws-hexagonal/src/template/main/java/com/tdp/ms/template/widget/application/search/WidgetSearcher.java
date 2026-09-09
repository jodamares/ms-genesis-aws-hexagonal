package com.tdp.ms.template.widget.application.search;

import com.tdp.ms.shared.domain.error.InvalidInputException;
import com.tdp.ms.shared.domain.error.ProcessingException;
import com.tdp.ms.template.widget.domain.ProductLine;
import com.tdp.ms.template.widget.domain.ProductWidget;
import com.tdp.ms.template.widget.domain.WidgetDefinition;
import com.tdp.ms.template.widget.domain.WidgetElement;
import com.tdp.ms.template.widget.domain.WidgetGateway;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WidgetSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(WidgetSearcher.class);
    private static final String OPERATION = "template.widgets.search";
    private static final int PRODUCT_LINE_ELEMENT_ID = 3;

    private final WidgetGateway gateway;

    public WidgetSearcher(WidgetGateway gateway) {
        this.gateway = gateway;
    }

    public Mono<SearchWidgetsResponse> search(String productIdValue, String productPs, String correlationId) {
        return Mono.defer(() -> {
            Integer productId = parseProductId(productIdValue);
            if (productPs == null || productPs.isBlank()) {
                return Mono.error(new InvalidInputException("TEMPLATE_PRODUCT_PS_REQUIRED", "productPS is required"));
            }

            return gateway.findProductWidgets(productId)
                    .collectList()
                    .filter(productWidgets -> !productWidgets.isEmpty())
                    .switchIfEmpty(Mono.error(new ProcessingException("TEMPLATE_PRODUCT_WIDGETS_NOT_AVAILABLE", "Product widgets are not available")))
                    .flatMap(productWidgets -> buildResponse(productId, productPs, productWidgets))
                    .doOnSuccess(response -> LOG.info(
                            "operation={} correlationId={} aggregateId={} entityId={} idempotencyKey=unavailable outcome=completed widgetCount={}",
                            OPERATION, correlationId, productId, productId, response.widgetList().size()))
                    .doOnError(error -> LOG.warn(
                            "operation={} correlationId={} aggregateId={} entityId={} idempotencyKey=unavailable outcome=rejected cause={}",
                            OPERATION, correlationId, productId, productId, error.getClass().getSimpleName()));
        });
    }

    private Mono<SearchWidgetsResponse> buildResponse(Integer productId, String productPs, List<ProductWidget> productWidgets) {
        List<Integer> widgetIds = productWidgets.stream()
                .map(ProductWidget::widgetId)
                .distinct()
                .toList();

        return Mono.zip(
                        gateway.findWidgets(widgetIds).collectList(),
                        gateway.findElements(widgetIds).collectList(),
                        gateway.findProductLines(productPs).collectList())
                .map(tuple -> toResponse(productWidgets, tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private SearchWidgetsResponse toResponse(
            List<ProductWidget> productWidgets,
            List<WidgetDefinition> widgetDefinitions,
            List<WidgetElement> elements,
            List<ProductLine> productLines) {
        Map<Integer, ProductWidget> relationByWidgetId = productWidgets.stream()
                .collect(Collectors.toMap(ProductWidget::widgetId, Function.identity(), (current, next) -> current));
        List<WidgetElement> orderedElements = elements.stream()
                .sorted(Comparator.comparing(WidgetElement::id))
                .toList();
        List<WidgetResponse> widgets = widgetDefinitions.stream()
                .map(widget -> toWidgetResponse(widget, relationByWidgetId.get(widget.id()), orderedElements, productLines))
                .sorted(Comparator.comparing(WidgetResponse::order))
                .toList();
        return new SearchWidgetsResponse(widgets);
    }

    private WidgetResponse toWidgetResponse(
            WidgetDefinition widget,
            ProductWidget productWidget,
            List<WidgetElement> elements,
            List<ProductLine> productLines) {
        List<WidgetElementResponse> elementResponses = elements.stream()
                .filter(element -> element.widgetId().equals(widget.id()))
                .map(element -> toElementResponse(element, productLines))
                .toList();

        return new WidgetResponse(
                widget.id(),
                widget.name(),
                widget.title(),
                widget.description(),
                elementResponses,
                productWidget == null ? 0 : productWidget.order(),
                productWidget != null && productWidget.enabled());
    }

    private WidgetElementResponse toElementResponse(WidgetElement element, List<ProductLine> productLines) {
        boolean enabled = element.id() == PRODUCT_LINE_ELEMENT_ID && productLines.isEmpty()
                ? false
                : element.enabled();
        return new WidgetElementResponse(element.id(), element.code(), element.widgetId(), element.name(), enabled);
    }

    private Integer parseProductId(String productIdValue) {
        if (productIdValue == null || productIdValue.isBlank()) {
            throw new InvalidInputException("TEMPLATE_PRODUCT_ID_REQUIRED", "productID is required");
        }
        try {
            return Integer.valueOf(productIdValue);
        } catch (NumberFormatException error) {
            throw new InvalidInputException("TEMPLATE_PRODUCT_ID_INVALID", "productID must be numeric");
        }
    }
}
