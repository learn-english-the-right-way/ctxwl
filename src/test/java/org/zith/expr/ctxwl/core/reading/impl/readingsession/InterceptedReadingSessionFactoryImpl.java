package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import org.hibernate.SessionFactory;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupRepository;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InterceptedReadingSessionFactoryImpl extends ReadingSessionFactoryImpl {
    private final ConcurrentLinkedQueue<Interceptor> interceptors;

    public InterceptedReadingSessionFactoryImpl(
            ComponentFactory componentFactory,
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            ReadingInspiredLookupRepository readingInspiredLookupRepository,
            Clock clock
    ) {
        super(componentFactory, sessionFactory, readingHistoryEntryRepository, readingInspiredLookupRepository, clock);
        interceptors = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void interceptInsertion(ReadingSessionEntity entity) {
        interceptors.forEach(i -> i.interceptInsertion(entity));
        super.interceptInsertion(entity);
    }

    @Override
    protected void interceptCreatingSession(ReadingSessionImpl readingSession) {
        interceptors.forEach(i -> i.interceptCreatingSession(readingSession));
        super.interceptCreatingSession(readingSession);
    }

    public static InterceptedReadingSessionFactoryImpl create(
            ComponentFactory componentFactory,
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            ReadingInspiredLookupRepository readingInspiredLookupRepository,
            Clock clock
    ) {
        return new InterceptedReadingSessionFactoryImpl(
                componentFactory,
                sessionFactory,
                readingHistoryEntryRepository,
                readingInspiredLookupRepository,
                clock
        );
    }

    Optional<Interceptor.Cancellation> inject(Interceptor interceptor) {
        interceptors.add(interceptor);
        return Optional.of(new SimpleCancellation(interceptor));
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
        void interceptCreatingSession(ReadingSessionImpl readingSession);

        void interceptInsertion(ReadingSessionEntity entity);

        interface Cancellation {
            void cancel();
        }
    }
}
