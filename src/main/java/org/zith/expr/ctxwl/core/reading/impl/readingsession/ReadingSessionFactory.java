package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

public interface ReadingSessionFactory {
    @NotNull ReadingSession makeSession(String group, String wordlist);

    Optional<ReadingSession> loadSession(String group, long serial);

    Flow.Publisher<Tracked<ReadingEvent>> collect(Executor executor);
}
