package com.tdp.ms.template.settings.infrastructure.r2dbc;

import com.tdp.ms.template.settings.domain.GeneralSetting;
import com.tdp.ms.template.settings.domain.SettingsGateway;
import io.r2dbc.spi.Row;
import java.util.Collection;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class R2dbcSettingsGateway implements SettingsGateway {
    private static final String FIND_BY_KEYS_SQL = """
            SELECT
                ac_general_settings_id AS id,
                ac_general_settings_key AS setting_key,
                ac_general_settings_value AS setting_value
            FROM public.ac_general_settings
            WHERE ac_general_settings_key IN (:keys)
            """;

    private final DatabaseClient databaseClient;

    public R2dbcSettingsGateway(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<GeneralSetting> findByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Flux.empty();
        }
        return databaseClient.sql(FIND_BY_KEYS_SQL)
                .bind("keys", keys)
                .map((row, metadata) -> toDomain(row))
                .all();
    }

    private GeneralSetting toDomain(Row row) {
        return new GeneralSetting(
                row.get("id", Integer.class),
                row.get("setting_key", String.class),
                row.get("setting_value", String.class));
    }
}
