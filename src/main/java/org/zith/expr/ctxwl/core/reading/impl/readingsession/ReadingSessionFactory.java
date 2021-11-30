package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.util.Optional;

public interface ReadingSessionFactory {
    @NotNull ReadingSession makeSession(String group);

    Optional<ReadingSession> loadSession(String group, long serial);
}
