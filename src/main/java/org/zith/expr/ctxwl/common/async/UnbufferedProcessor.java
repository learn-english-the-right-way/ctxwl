package org.zith.expr.ctxwl.common.async;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.function.Function;

public final class UnbufferedProcessor<T, R> implements Flow.Processor<T, R> {
    private final Strand strand;
    private final Function<T, R> mapper;
    private final State state;

    public UnbufferedProcessor(Executor executor, Function<T, R> mapper) {
        this.strand = new Strand(Objects.requireNonNull(executor));
        this.mapper = Objects.requireNonNull(mapper);
        this.state = new State();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super R> subscriber) {
        Exception failed = null;
        Flow.Subscription deferredSubscription;
        Throwable pendingError;
        boolean pendingCompletion;
        synchronized (state) {
            if (state.downstream != null) {
                failed = new IllegalStateException("This publisher has already been subscribed");
            }
            state.downstream = subscriber;
            deferredSubscription = state.deferredSubscription;
            pendingError = state.pendingError;
            pendingCompletion = state.pendingCompletion;
            state.deferredSubscription = null;
            state.pendingError = null;
            state.pendingCompletion = false;
        }

        if (failed != null) {
            Exception failure = failed;
            strand.execute(() -> subscriber.onError(failure));
            return;
        }

        if (pendingError != null) {
            passError(subscriber, pendingError);
        } else {
            if (pendingCompletion) {
                passSubscription(subscriber, NoopSubscription.getInstance());
                passCompletion(subscriber);
            } else if (deferredSubscription != null) {
                passSubscription(subscriber, deferredSubscription);
            }
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        var downstream = state.downstream;
        if (downstream == null) {
            synchronized (state) {
                downstream = state.downstream;
                if (downstream == null) {
                    if (state.pendingError == null && !state.pendingCompletion) {
                        state.deferredSubscription = subscription;
                    }
                }
            }
        }

        if (downstream != null) {
            passSubscription(downstream, subscription);
        }
    }

    @Override
    public void onNext(T item) {
        var downstream = state.downstream;
        if (downstream == null) {
            return;
        }
        strand.execute(() -> downstream.onNext(mapper.apply(item)));
    }

    @Override
    public void onError(Throwable throwable) {
        var downstream = state.downstream;
        if (downstream == null) {
            synchronized (state) {
                downstream = state.downstream;
                if (downstream == null) {
                    state.deferredSubscription = null;
                    state.pendingError = throwable;
                    state.pendingCompletion = false;
                    return;
                }
            }
        }
        passError(downstream, throwable);
    }

    @Override
    public void onComplete() {
        var downstream = state.downstream;
        if (downstream == null) {
            synchronized (state) {
                downstream = state.downstream;
                if (downstream == null) {
                    if (state.pendingError == null) {
                        state.deferredSubscription = null;
                        state.pendingCompletion = true;
                    }
                    return;
                }
            }
        }
        passCompletion(downstream);
    }

    private void passSubscription(
            @NotNull Flow.Subscriber<? super R> downstream,
            @NotNull Flow.Subscription subscription
    ) {
        strand.execute(() -> downstream.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                subscription.request(n);
            }

            @Override
            public void cancel() {
                subscription.cancel();
            }
        }));
    }

    private void passError(
            @NotNull Flow.Subscriber<? super R> downstream,
            @NotNull Throwable throwable
    ) {
        strand.execute(() -> downstream.onError(throwable));
    }

    private void passCompletion(
            @NotNull Flow.Subscriber<? super R> downstream
    ) {
        strand.execute(downstream::onComplete);
    }

    private class State {
        volatile Flow.Subscriber<? super R> downstream;
        Flow.Subscription deferredSubscription;
        Throwable pendingError;
        boolean pendingCompletion;
    }
}
