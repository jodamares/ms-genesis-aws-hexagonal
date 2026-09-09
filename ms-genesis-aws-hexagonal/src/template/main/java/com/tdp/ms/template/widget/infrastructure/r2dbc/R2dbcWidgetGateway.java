package com.tdp.ms.template.widget.infrastructure.r2dbc;

import com.tdp.ms.template.widget.domain.ProductLine;
import com.tdp.ms.template.widget.domain.ProductWidget;
import com.tdp.ms.template.widget.domain.WidgetDefinition;
import com.tdp.ms.template.widget.domain.WidgetElement;
import com.tdp.ms.template.widget.domain.WidgetGateway;
import io.r2dbc.spi.Row;
import java.util.Collection;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class R2dbcWidgetGateway implements WidgetGateway {
    private static final String FIND_PRODUCT_WIDGETS_SQL = """
            SELECT
                product_widget_id,
                product_id,
                widget_id,
                product_widget_state,
                product_widget_order
            FROM public.ac_product_widget
            WHERE product_id = :productId
            """;

    private static final String FIND_WIDGETS_SQL = """
            SELECT
                widget_id,
                widget_name,
                widget_title,
                widget_description
            FROM public.ac_widget
            WHERE widget_id IN (:widgetIds)
            """;

    private static final String FIND_ELEMENTS_SQL = """
            SELECT
                widget_element_id,
                ac_widget_element_code,
                widget_id,
                element_name,
                element_state
            FROM public.ac_widget_element
            WHERE widget_id IN (:widgetIds)
            """;

    private static final String FIND_PRODUCT_LINES_SQL = """
            SELECT id, ps_code
            FROM public.ac_c2c_line_ps_list
            WHERE ps_code = :psCode
            """;

    private final DatabaseClient databaseClient;

    public R2dbcWidgetGateway(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<ProductWidget> findProductWidgets(Integer productId) {
        return databaseClient.sql(FIND_PRODUCT_WIDGETS_SQL)
                .bind("productId", productId)
                .map((row, metadata) -> toProductWidget(row))
                .all();
    }

    @Override
    public Flux<WidgetDefinition> findWidgets(Collection<Integer> widgetIds) {
        if (widgetIds == null || widgetIds.isEmpty()) {
            return Flux.empty();
        }
        return databaseClient.sql(FIND_WIDGETS_SQL)
                .bind("widgetIds", widgetIds)
                .map((row, metadata) -> toWidgetDefinition(row))
                .all();
    }

    @Override
    public Flux<WidgetElement> findElements(Collection<Integer> widgetIds) {
        if (widgetIds == null || widgetIds.isEmpty()) {
            return Flux.empty();
        }
        return databaseClient.sql(FIND_ELEMENTS_SQL)
                .bind("widgetIds", widgetIds)
                .map((row, metadata) -> toWidgetElement(row))
                .all();
    }

    @Override
    public Flux<ProductLine> findProductLines(String psCode) {
        return databaseClient.sql(FIND_PRODUCT_LINES_SQL)
                .bind("psCode", psCode)
                .map((row, metadata) -> toProductLine(row))
                .all();
    }

    private ProductWidget toProductWidget(Row row) {
        return new ProductWidget(
                row.get("product_widget_id", Integer.class),
                row.get("product_id", Integer.class),
                row.get("widget_id", Integer.class),
                Boolean.TRUE.equals(row.get("product_widget_state", Boolean.class)),
                row.get("product_widget_order", Integer.class));
    }

    private WidgetDefinition toWidgetDefinition(Row row) {
        return new WidgetDefinition(
                row.get("widget_id", Integer.class),
                row.get("widget_name", String.class),
                row.get("widget_title", String.class),
                row.get("widget_description", String.class));
    }

    private WidgetElement toWidgetElement(Row row) {
        return new WidgetElement(
                row.get("widget_element_id", Integer.class),
                row.get("ac_widget_element_code", String.class),
                row.get("widget_id", Integer.class),
                row.get("element_name", String.class),
                Boolean.TRUE.equals(row.get("element_state", Boolean.class)));
    }

    private ProductLine toProductLine(Row row) {
        return new ProductLine(row.get("id", Long.class), row.get("ps_code", String.class));
    }
}
