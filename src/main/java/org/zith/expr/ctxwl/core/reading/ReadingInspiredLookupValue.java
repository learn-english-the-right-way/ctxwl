package org.zith.expr.ctxwl.core.reading;

import java.time.Instant;
import java.util.Optional;

public record ReadingInspiredLookupValue(
        String criterion,
        Optional<Long> offset,
        Optional<Instant> creationTime
) implements ReadingInspiredLookupValueLike {
}
