package org.zith.expr.ctxwl.core.reading;

import org.zith.expr.ctxwl.common.async.Tracked;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

public interface ReadingService extends AutoCloseable {
    ReadingSession makeSession(String group, String wordlist);

    Optional<ReadingSession> loadSession(String group, long serial);

    Flow.Publisher<Tracked<ReadingEvent>> collect(Executor executor);

    ReadingInducedWordlist getWordlist(String id);

    void extendWordlist(List<ReadingEvent> events);

    @Override
    void close();
}
