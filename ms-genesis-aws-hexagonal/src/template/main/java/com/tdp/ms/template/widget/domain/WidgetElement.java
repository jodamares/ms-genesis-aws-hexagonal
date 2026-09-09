package com.tdp.ms.template.widget.domain;

public record WidgetElement(Integer id, String code, Integer widgetId, String name, boolean enabled) {
    public WidgetElement withEnabled(boolean newEnabled) {
        return new WidgetElement(id, code, widgetId, name, newEnabled);
    }
}
