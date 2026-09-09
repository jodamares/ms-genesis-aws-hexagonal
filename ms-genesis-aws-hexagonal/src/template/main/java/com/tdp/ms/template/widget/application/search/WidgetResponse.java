package com.tdp.ms.template.widget.application.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WidgetResponse(
        @JsonProperty("widgetID") Integer widgetId,
        String name,
        String title,
        String description,
        List<WidgetElementResponse> elements,
        Integer order,
        Boolean status) {
}
