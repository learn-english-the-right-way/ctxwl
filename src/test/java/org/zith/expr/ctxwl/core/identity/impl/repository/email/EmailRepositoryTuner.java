package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import org.zith.expr.ctxwl.core.identity.EmailRepository;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@NotThreadSafe
public class EmailRepositoryTuner implements AutoCloseable {
    private final InterceptedEmailRepositoryImpl.Interceptor interceptor;
    private final ThreadLocal<Context> context;
    private final AtomicReference<Consumer<EmailEntity>> callbackOnInsertion;
    private InterceptedEmailRepositoryImpl.Interceptor.Cancellation cancellation;

    public EmailRepositoryTuner() {
        context = new ThreadLocal<>();
        interceptor = new Interceptor();
        callbackOnInsertion = new AtomicReference<>();
    }

    public boolean intercept(EmailRepository emailRepository) {
        if (emailRepository instanceof InterceptedEmailRepositoryImpl) {
            return intercept((InterceptedEmailRepositoryImpl) emailRepository);
        } else {
            return false;
        }
    }

    public boolean intercept(InterceptedEmailRepositoryImpl emailRepository) {
        context.set(new Context());

        var optionalCancellation = emailRepository.inject(interceptor);

        if (optionalCancellation.isEmpty()) {
            return false;
        }

        cancellation = optionalCancellation.get();

        return true;
    }

    @Override
    public void close() {
        Optional.ofNullable(cancellation).ifPresent(InterceptedEmailRepositoryImpl.Interceptor.Cancellation::cancel);
        cancellation = null;
    }

    public void onInsertion(Consumer<EmailEntity> callbackOnInsertion) {
        this.callbackOnInsertion.set(callbackOnInsertion);
    }

    private class Context {
        public void interceptInsertion(EmailEntity freshEntity) {
            Optional.ofNullable(callbackOnInsertion.getAndSet(null)).ifPresent(c -> c.accept(freshEntity));
        }
    }

    private class Interceptor implements InterceptedEmailRepositoryImpl.Interceptor {
        @Override
        public void interceptInsertion(EmailEntity freshEntity) {
            Optional.ofNullable(context.get()).ifPresent(c -> c.interceptInsertion(freshEntity));
        }
    }
}
