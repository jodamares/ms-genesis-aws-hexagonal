package com.tdp.ms.template.widget.application.search;

import com.tdp.ms.shared.domain.bus.query.Response;
import java.util.List;

public record SearchWidgetsResponse(List<WidgetResponse> widgetList) implements Response {
}
