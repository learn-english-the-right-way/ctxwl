package org.zith.expr.ctxwl.core.reading;

import java.util.Optional;

public interface ReadingInspiredLookup extends ReadingInspiredLookupValueLike {

    ReadingSession session();

    Optional<ReadingHistoryEntry> historyEntry();

    long serial();

    long historyEntrySerial();

}
