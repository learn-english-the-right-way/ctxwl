package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.util.Optional;

public class ReadingSessionTuner implements AutoCloseable {
    private final InterceptedReadingSessionImpl.Interceptor interceptor;
    private final ThreadLocal<Context> context;
    private InterceptedReadingSessionImpl.Interceptor.Cancellation cancellation;

    public ReadingSessionTuner() {
        interceptor = new Interceptor();
        context = new ThreadLocal<>();
    }

    public boolean tune(ReadingSession readingSession) {
        if (readingSession instanceof InterceptedReadingSessionImpl) {
            return tune((InterceptedReadingSessionImpl) readingSession);
        } else {
            return false;
        }
    }

    public boolean tune(InterceptedReadingSessionImpl readingSession) {
        context.set(new Context());

        var optionalCancellation = readingSession.inject(interceptor);

        if (optionalCancellation.isEmpty()) {
            return false;
        }

        cancellation = optionalCancellation.get();

        return true;
    }

    @Override
    public void close() {
        Optional.ofNullable(cancellation).ifPresent(InterceptedReadingSessionImpl.Interceptor.Cancellation::cancel);
    }

    private class Context {

    }

    private class Interceptor implements InterceptedReadingSessionImpl.Interceptor {
    }
}
