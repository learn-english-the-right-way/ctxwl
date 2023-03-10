package org.zith.expr.ctxwl.core.reading.impl.common;

import org.zith.expr.ctxwl.core.reading.ReadingSession;

public interface SessionProvider<Session extends ReadingSession> {
    Session getSession(String group, long serial);
}
