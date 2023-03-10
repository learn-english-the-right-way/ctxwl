package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NotThreadSafe
public class ReadingSessionFactoryTuner implements AutoCloseable {
    private final Interceptor interceptor;
    private final ThreadLocal<Context> context;
    private final ConcurrentLinkedQueue<ReadingSessionTuner> readingSessionTuners;
    private final AtomicReference<Consumer<ReadingSessionEntity>> callbackOnInsertion;
    private final AtomicReference<BiConsumer<ReadingSessionEntity, ReadingSessionEntity>> callbackOnRotation;
    private InterceptedReadingSessionFactoryImpl.Interceptor.Cancellation cancellation;

    public ReadingSessionFactoryTuner() {
        interceptor = new Interceptor();
        context = new ThreadLocal<>();
        readingSessionTuners = new ConcurrentLinkedQueue<>();
        callbackOnInsertion = new AtomicReference<>();
        callbackOnRotation = new AtomicReference<>();
    }

    public boolean tune(ReadingSessionFactory readingSessionFactory) {
        if (readingSessionFactory instanceof InterceptedReadingSessionFactoryImpl) {
            return tune((InterceptedReadingSessionFactoryImpl) readingSessionFactory);
        } else {
            return false;
        }
    }

    public boolean tune(InterceptedReadingSessionFactoryImpl readingSessionFactory) {
        context.set(new Context());

        var optionalCancellation = readingSessionFactory.inject(interceptor);

        if (optionalCancellation.isEmpty()) {
            return false;
        }

        cancellation = optionalCancellation.get();

        return true;
    }

    public void onInsertion(Consumer<ReadingSessionEntity> callbackOnInsertion) {
        this.callbackOnInsertion.set(callbackOnInsertion);
    }

    @Override
    public void close() {
        readingSessionTuners.forEach(ReadingSessionTuner::close);
        Optional.ofNullable(cancellation)
                .ifPresent(InterceptedReadingSessionFactoryImpl.Interceptor.Cancellation::cancel);
    }

    private class Context {
        public void interceptInsertion(ReadingSessionEntity entity) {
            Optional.ofNullable(callbackOnInsertion.getAndSet(null)).ifPresent(c -> c.accept(entity));
        }

        public void interceptRotation(ReadingSessionEntity entity, ReadingSessionEntity placeholderEntity) {
            Optional.ofNullable(callbackOnRotation.getAndSet(null))
                    .ifPresent(c -> c.accept(entity, placeholderEntity));
        }
    }

    private class Interceptor implements InterceptedReadingSessionFactoryImpl.Interceptor {
        @Override
        public void interceptCreatingSession(ReadingSessionImpl readingSession) {
            var readingSessionTuner = new ReadingSessionTuner();
            if (readingSessionTuner.tune(readingSession)) {
                readingSessionTuners.add(readingSessionTuner);
            } else {
                readingSessionTuner.close();
            }
        }

        @Override
        public void interceptInsertion(ReadingSessionEntity entity) {
            Optional.ofNullable(context.get()).ifPresent(c -> c.interceptInsertion(entity));
        }

        @Override
        public void interceptRotation(ReadingSessionEntity entity, ReadingSessionEntity placeholderEntity) {
            Optional.ofNullable(context.get()).ifPresent(c -> c.interceptRotation(entity, placeholderEntity));
        }
    }
}
