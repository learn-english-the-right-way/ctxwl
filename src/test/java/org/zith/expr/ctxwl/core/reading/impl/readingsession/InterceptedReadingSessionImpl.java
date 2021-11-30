package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InterceptedReadingSessionImpl extends ReadingSessionImpl {
    private final ConcurrentLinkedQueue<Interceptor> interceptors;

    public InterceptedReadingSessionImpl(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity entity
    ) {
        super(readingSessionFactory, entity);
        interceptors = new ConcurrentLinkedQueue<>();
    }

    public static InterceptedReadingSessionImpl create(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity entity
    ) {
        return new InterceptedReadingSessionImpl(readingSessionFactory, entity);
    }

    public Optional<Interceptor.Cancellation> inject(Interceptor interceptor) {
        interceptors.add(interceptor);
        var cancellation = new SimpleCancellation(interceptor);
        return Optional.of(cancellation);
    }

    private class SimpleCancellation implements Interceptor.Cancellation {

        private final Interceptor interceptor;

        public SimpleCancellation(Interceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void cancel() {
            interceptors.remove(interceptor);
        }
    }

    interface Interceptor {
        interface Cancellation {
            void cancel();
        }
    }
}
