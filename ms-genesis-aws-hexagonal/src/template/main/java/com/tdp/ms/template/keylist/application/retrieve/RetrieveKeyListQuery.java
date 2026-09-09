package com.tdp.ms.template.keylist.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.Query;

public record RetrieveKeyListQuery(String key, String correlationId) implements Query {
}
