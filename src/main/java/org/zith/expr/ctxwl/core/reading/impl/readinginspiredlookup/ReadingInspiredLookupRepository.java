package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

public interface ReadingInspiredLookupRepository {
    <Session extends ReadingSession>
    BoundReadingInspiredLookup<Session> create(
            Session session,
            long historyEntrySerial,
            long serial,
            ReadingInspiredLookupValue readingInspiredLookupValue
    );

    <Session extends ReadingSession>
    Flow.Publisher<Tracked<BoundReadingInspiredLookup<Session>>>
    collect(Executor executor, SessionProvider<Session> sessionProvider);
}
