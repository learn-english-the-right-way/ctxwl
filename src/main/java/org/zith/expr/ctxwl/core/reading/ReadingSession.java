package org.zith.expr.ctxwl.core.reading;

import java.time.Instant;
import java.util.Optional;

public interface ReadingSession {
    String getGroup();

    long getSerial();

    void complete(Instant timestamp);

    ReadingHistoryEntry create(long serial, ReadingHistoryEntryValue template);

    Optional<Instant> getUpdateTime();

    Optional<Instant> getCompletionTime();

    Optional<Instant> getTerminationTime();
}
