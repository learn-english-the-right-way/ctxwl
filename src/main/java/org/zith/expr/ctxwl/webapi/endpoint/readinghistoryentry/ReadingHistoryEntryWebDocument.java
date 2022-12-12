package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ReadingHistoryEntryWebDocument(
        String session,
        Long serial,
        String uri,
        Optional<String> text,
        Optional<Instant> creationTime,
        Optional<Instant> updateTime,
        Optional<Long> majorSerial) {
}
