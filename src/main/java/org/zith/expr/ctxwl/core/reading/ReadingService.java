package org.zith.expr.ctxwl.core.reading;

import java.util.Optional;

public interface ReadingService extends AutoCloseable {
    ReadingSession makeSession(String group);

    Optional<ReadingSession> loadSession(String group, long serial);
}
