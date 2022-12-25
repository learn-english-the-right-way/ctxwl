package org.zith.expr.ctxwl.core.reading;

import java.time.Instant;
import java.util.Optional;

public interface ReadingInspiredLookupValueLike {
    String criterion();

    Optional<Long> offset();

    Optional<Instant> creationTime();
}
