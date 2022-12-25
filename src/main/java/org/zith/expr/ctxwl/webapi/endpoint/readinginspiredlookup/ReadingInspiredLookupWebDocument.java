package org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ReadingInspiredLookupWebDocument(
        String session,
        Long entrySerial,
        Long serial,
        String criterion,
        Optional<Long> offset,
        Optional<Instant> creationTime
) {
}
