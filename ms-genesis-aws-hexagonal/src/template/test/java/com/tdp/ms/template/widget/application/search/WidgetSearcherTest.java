package com.tdp.ms.template.widget.application.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tdp.ms.shared.domain.error.InvalidInputException;
import com.tdp.ms.shared.domain.error.ProcessingException;
import com.tdp.ms.template.widget.domain.ProductLine;
import com.tdp.ms.template.widget.domain.ProductWidget;
import com.tdp.ms.template.widget.domain.WidgetDefinition;
import com.tdp.ms.template.widget.domain.WidgetElement;
import com.tdp.ms.template.widget.domain.WidgetGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WidgetSearcherTest {
    @Mock
    WidgetGateway gateway;

    @Test
    void shouldSearchWidgetsAndDisableProductLineElementWhenProductLineIsMissing() {
        when(gateway.findProductWidgets(10)).thenReturn(Flux.just(
                new ProductWidget(1, 10, 20, true, 2),
                new ProductWidget(2, 10, 30, false, 1)));
        when(gateway.findWidgets(java.util.List.of(20, 30))).thenReturn(Flux.just(
                new WidgetDefinition(20, "BalanceComponent", "Balance", "Balance detail"),
                new WidgetDefinition(30, "PlanComponent", "Plan", "Plan detail")));
        when(gateway.findElements(java.util.List.of(20, 30))).thenReturn(Flux.just(
                new WidgetElement(3, "PRODUCT_LINE", 20, "Product line", true),
                new WidgetElement(1, "DETAIL", 30, "Detail", true)));
        when(gateway.findProductLines("PS-1")).thenReturn(Flux.empty());

        WidgetSearcher searcher = new WidgetSearcher(gateway);

        StepVerifier.create(searcher.search("10", "PS-1", "corr-1"))
                .assertNext(response -> {
                    assertEquals(2, response.widgetList().size());
                    assertEquals(30, response.widgetList().get(0).widgetId());
                    assertFalse(response.widgetList().get(0).status());
                    assertEquals(20, response.widgetList().get(1).widgetId());
                    assertFalse(response.widgetList().get(1).elements().get(0).elementState());
                })
                .verifyComplete();
    }

    @Test
    void shouldKeepProductLineElementEnabledWhenProductLineExists() {
        when(gateway.findProductWidgets(10)).thenReturn(Flux.just(new ProductWidget(1, 10, 20, true, 1)));
        when(gateway.findWidgets(java.util.List.of(20))).thenReturn(Flux.just(new WidgetDefinition(20, "BalanceComponent", "Balance", "Balance detail")));
        when(gateway.findElements(java.util.List.of(20))).thenReturn(Flux.just(new WidgetElement(3, "PRODUCT_LINE", 20, "Product line", true)));
        when(gateway.findProductLines("PS-1")).thenReturn(Flux.just(new ProductLine(1L, "PS-1")));

        WidgetSearcher searcher = new WidgetSearcher(gateway);

        StepVerifier.create(searcher.search("10", "PS-1", "corr-1"))
                .assertNext(response -> assertTrue(response.widgetList().get(0).elements().get(0).elementState()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductWidgetsAreMissing() {
        when(gateway.findProductWidgets(10)).thenReturn(Flux.empty());

        WidgetSearcher searcher = new WidgetSearcher(gateway);

        StepVerifier.create(searcher.search("10", "PS-1", "corr-1"))
                .expectError(ProcessingException.class)
                .verify();
    }

    @Test
    void shouldFailWhenProductIdIsInvalid() {
        WidgetSearcher searcher = new WidgetSearcher(gateway);

        StepVerifier.create(searcher.search("abc", "PS-1", "corr-1"))
                .expectError(InvalidInputException.class)
                .verify();
    }
}
