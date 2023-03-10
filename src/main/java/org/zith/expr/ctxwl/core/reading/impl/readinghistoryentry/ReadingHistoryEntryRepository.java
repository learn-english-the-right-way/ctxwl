package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.bson.types.ObjectId;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;

public interface ReadingHistoryEntryRepository {
    <Session extends ReadingSession>
    BoundReadingHistoryEntry<Session> create(Session session, long serial, ReadingHistoryEntryValue value);

    <Session extends ReadingSession>
    BoundReadingHistoryEntry<Session> get(Session session, long serial);

    <Session extends ReadingSession> ObjectId ensureReference(Session session, long serial);

    <Session extends ReadingSession> BoundReadingHistoryEntry<Session> resolveReference(
            SessionProvider<Session> sessionProvider,
            ObjectId reference
    );

    <Session extends ReadingSession>
    BoundReadingHistoryEntry<Session> get(Session session, long serial, ObjectId reference);
}
