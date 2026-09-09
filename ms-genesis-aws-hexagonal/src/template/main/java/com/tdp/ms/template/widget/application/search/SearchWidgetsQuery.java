package com.tdp.ms.template.widget.application.search;

import com.tdp.ms.shared.domain.bus.query.Query;

public record SearchWidgetsQuery(String productId, String productPs, String correlationId) implements Query {
}
