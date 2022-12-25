package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

public interface ReadingInspiredLookupRepository {
    <Session extends ReadingSession>
    BoundReadingInspiredLookup<Session> create(
            Session session,
            long historyEntrySerial,
            long serial,
            ReadingInspiredLookupValue readingInspiredLookupValue
    );
}
