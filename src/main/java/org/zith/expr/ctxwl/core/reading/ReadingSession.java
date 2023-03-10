package org.zith.expr.ctxwl.core.reading;

import java.time.Instant;
import java.util.Optional;

public interface ReadingSession extends AutoCloseable {
    String getGroup();

    long getSerial();

    void complete(Instant timestamp);

    ReadingHistoryEntry createHistoryEntry(long serial, ReadingHistoryEntryValue template);

    ReadingInspiredLookup createLookup(
            long historyEntrySerial,
            long serial,
            ReadingInspiredLookupValue readingInspiredLookupValue
    );

    String getWordlist();

    Optional<Instant> getUpdateTime();

    Optional<Instant> getCompletionTime();

    Optional<Instant> getTerminationTime();

    @Override
    void close();
}
