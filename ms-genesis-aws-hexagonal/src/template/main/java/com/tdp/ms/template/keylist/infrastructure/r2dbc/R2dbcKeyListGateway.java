package com.tdp.ms.template.keylist.infrastructure.r2dbc;

import com.tdp.ms.shared.domain.error.ProcessingException;
import com.tdp.ms.template.keylist.domain.KeyList;
import com.tdp.ms.template.keylist.domain.KeyListDetail;
import com.tdp.ms.template.keylist.domain.KeyListGateway;
import io.r2dbc.spi.Row;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcKeyListGateway implements KeyListGateway {
    private static final String SELECT_BY_DESCRIPTION_SQL = """
            SELECT
                kl.id AS kl_id,
                kl.description AS kl_description,
                kld.id AS kld_id,
                kld."key" AS kld_key,
                kld."value" AS kld_value,
                kld.data_type AS kld_data_type
            FROM public.ac_keylist kl
            LEFT JOIN public.ac_keylist_detail kld ON kld.keylist_id = kl.id
            WHERE kl.description = :description
            ORDER BY kl.id, kld.id
            """;

    private final DatabaseClient databaseClient;

    public R2dbcKeyListGateway(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<KeyList> findByDescription(String description) {
        return databaseClient.sql(SELECT_BY_DESCRIPTION_SQL)
                .bind("description", description)
                .map((row, metadata) -> toRow(row))
                .all()
                .collectList()
                .flatMap(this::toKeyList);
    }

    private Mono<KeyList> toKeyList(List<KeyListRow> rows) {
        if (rows.isEmpty()) {
            return Mono.empty();
        }
        long keyListCount = rows.stream().map(KeyListRow::keyListId).distinct().count();
        if (keyListCount != 1) {
            return Mono.error(new ProcessingException("TEMPLATE_KEY_LIST_DUPLICATED", "Key list description returns multiple rows"));
        }

        KeyListRow first = rows.get(0);
        Set<KeyListDetail> details = new LinkedHashSet<>();
        rows.stream()
                .filter(row -> row.detailId() != null)
                .filter(row -> details.stream().noneMatch(detail -> Objects.equals(detail.id(), row.detailId())))
                .map(row -> new KeyListDetail(row.detailId(), row.detailKey(), row.detailValue(), row.detailDataType()))
                .forEach(details::add);

        return Mono.just(new KeyList(first.keyListId(), first.keyListDescription(), details));
    }

    private KeyListRow toRow(Row row) {
        return new KeyListRow(
                row.get("kl_id", Integer.class),
                row.get("kl_description", String.class),
                row.get("kld_id", Integer.class),
                row.get("kld_key", String.class),
                row.get("kld_value", String.class),
                row.get("kld_data_type", String.class));
    }

    private record KeyListRow(
            Integer keyListId,
            String keyListDescription,
            Integer detailId,
            String detailKey,
            String detailValue,
            String detailDataType) {
    }
}
