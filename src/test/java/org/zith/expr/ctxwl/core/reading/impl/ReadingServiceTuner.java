package org.zith.expr.ctxwl.core.reading.impl;

import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionEntity;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactory;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactoryTuner;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.function.Consumer;

@NotThreadSafe
public class ReadingServiceTuner implements AutoCloseable {

    private final Interceptor interceptor;
    private final ThreadLocal<Context> context;
    private ReadingSessionFactoryTuner readingSessionFactoryTuner;
    private InterceptedReadingServiceImpl.Interceptor.Cancellation cancellation;

    public ReadingServiceTuner() {
        interceptor = new Interceptor();
        context = new ThreadLocal<>();
    }

    public boolean tune(ReadingService readingService) {
        if (readingService instanceof InterceptedReadingServiceImpl) {
            return tune((InterceptedReadingServiceImpl) readingService);
        } else {
            return false;
        }
    }

    public boolean tune(InterceptedReadingServiceImpl readingService) {
        context.set(new Context());

        var optionalCancellation = readingService.inject(interceptor);

        if (optionalCancellation.isEmpty()) {
            return false;
        }

        cancellation = optionalCancellation.get();

        return true;
    }

    public void onSessionInsertion(Consumer<ReadingSessionEntity> callbackOnSessionInsertion) {
        Optional.ofNullable(readingSessionFactoryTuner).ifPresent(t -> t.onInsertion(callbackOnSessionInsertion));
    }

    @Override
    public void close() {
        Optional.ofNullable(readingSessionFactoryTuner).ifPresent(ReadingSessionFactoryTuner::close);
        Optional.ofNullable(cancellation).ifPresent(InterceptedReadingServiceImpl.Interceptor.Cancellation::cancel);
    }

    private class Context {
        public void configure(ReadingSessionFactory readingSessionFactory) {
            var readingSessionFactoryTuner = new ReadingSessionFactoryTuner();
            if (readingSessionFactoryTuner.tune(readingSessionFactory)) {
                ReadingServiceTuner.this.readingSessionFactoryTuner = readingSessionFactoryTuner;
            } else {
                readingSessionFactoryTuner.close();
            }
        }
    }

    private class Interceptor implements InterceptedReadingServiceImpl.Interceptor {
        @Override
        public void configure(ReadingSessionFactory readingSessionFactory) {
            Optional.ofNullable(context.get()).ifPresent(c -> c.configure(readingSessionFactory));
        }
    }
}
