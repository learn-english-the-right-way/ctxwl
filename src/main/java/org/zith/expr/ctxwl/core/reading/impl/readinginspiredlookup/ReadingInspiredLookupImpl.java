package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.time.Instant;
import java.util.Optional;

public class ReadingInspiredLookupImpl<Session extends ReadingSession> implements BoundReadingInspiredLookup<Session> {
    public static <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> create() {
        return null;
    }

    @Override
    public ReadingSession session() {
        return null;
    }

    @Override
    public Optional<ReadingHistoryEntry> readingHistoryEntry() {
        return Optional.empty();
    }

    @Override
    public long serial() {
        return 0;
    }

    @Override
    public long historyEntrySerial() {
        return 0;
    }

    @Override
    public String criterion() {
        return null;
    }

    @Override
    public Optional<Long> offset() {
        return Optional.empty();
    }

    @Override
    public Optional<Instant> creationTime() {
        return Optional.empty();
    }
}
