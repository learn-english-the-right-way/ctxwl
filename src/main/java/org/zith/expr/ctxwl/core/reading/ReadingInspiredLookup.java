package org.zith.expr.ctxwl.core.reading;

import java.util.Optional;

public interface ReadingInspiredLookup extends ReadingInspiredLookupValueLike {

    ReadingSession session();

    Optional<ReadingHistoryEntry> readingHistoryEntry();

    long serial();

    long historyEntrySerial();

}
