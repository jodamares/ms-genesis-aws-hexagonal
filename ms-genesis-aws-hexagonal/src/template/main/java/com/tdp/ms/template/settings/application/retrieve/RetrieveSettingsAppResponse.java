package com.tdp.ms.template.settings.application.retrieve;

import com.tdp.ms.shared.domain.bus.query.Response;
import java.util.List;
import java.util.Map;

public record RetrieveSettingsAppResponse(Map<String, String> settings, List<AdditionalSettingResponse> additionalData)
        implements Response {
}
