package org.zith.expr.ctxwl.core.reading;

import java.time.Instant;
import java.util.Optional;

public interface ReadingHistoryEntryValueLike {
    String uri();

    Optional<String> text();

    Optional<Instant> creationTime();

    Optional<Instant> updateTime();

    Optional<Long> majorSerial();
}
