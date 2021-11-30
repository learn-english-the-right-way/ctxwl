package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

public interface BoundReadingHistoryEntry<Session extends ReadingSession> extends ReadingHistoryEntry {
    @Override
    Session session();
}
