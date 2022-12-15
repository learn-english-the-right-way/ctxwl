package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

public interface ReadingHistoryEntryRepository {
    <Session extends ReadingSession>
    BoundReadingHistoryEntry<Session> create(Session session, long serial, ReadingHistoryEntryValue value);

    <Session extends ReadingSession>
    BoundReadingHistoryEntry<Session> get(Session session, long serial);

    void drop();
}
