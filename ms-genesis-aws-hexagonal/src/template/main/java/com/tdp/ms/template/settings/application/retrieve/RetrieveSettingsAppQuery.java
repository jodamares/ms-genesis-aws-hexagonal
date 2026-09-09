package com.tdp.ms.template.settings.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.Query;

public record RetrieveSettingsAppQuery(String correlationId) implements Query {
}
