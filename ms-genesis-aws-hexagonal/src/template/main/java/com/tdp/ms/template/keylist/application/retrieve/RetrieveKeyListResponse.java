package com.tdp.ms.template.keylist.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.Response;
import java.util.Set;

public record RetrieveKeyListResponse(String key, Set<KeyListDetailResponse> list) implements Response {
}
