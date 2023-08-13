package org.zith.expr.ctxwl.common.async;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Function;

public final class UnbufferedProcessor<T, R> implements Flow.Processor<T, R> {
    private final Function<T, R> mapper;
    private final State state;

    public UnbufferedProcessor(Function<T, R> mapper) {
        this.mapper = Objects.requireNonNull(mapper);
        this.state = new State();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super R> subscriber) {
        Exception subscriptionFailure = null;
        Flow.Subscription deferredSubscription;
        Throwable error;
        boolean completed;
        synchronized (state) {
            if (state.downstream != null) {
                subscriptionFailure = new IllegalStateException("This publisher has already been subscribed");
            }
            state.downstream = subscriber;
            deferredSubscription = state.deferredSubscription;
            error = state.error;
            completed = state.completed;
            state.deferredSubscription = null;
            state.error = null;
            state.completed = false;
        }

        if (subscriptionFailure != null) {
            subscriber.onError(subscriptionFailure);
            return;
        }

        if (error != null) {
            passError(subscriber, error);
        } else {
            if (completed) {
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
                    if (state.error == null && !state.completed) {
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
        downstream.onNext(mapper.apply(item));
    }

    @Override
    public void onError(Throwable throwable) {
        var downstream = state.downstream;
        if (downstream == null) {
            synchronized (state) {
                downstream = state.downstream;
                if (downstream == null) {
                    state.deferredSubscription = null;
                    state.error = throwable;
                    state.completed = false;
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
                    if (state.error == null) {
                        state.deferredSubscription = null;
                        state.completed = true;
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
        downstream.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                subscription.request(n);
            }

            @Override
            public void cancel() {
                subscription.cancel();
            }
        });
    }

    private void passError(
            @NotNull Flow.Subscriber<? super R> downstream,
            @NotNull Throwable throwable
    ) {
        downstream.onError(throwable);
    }

    private void passCompletion(
            @NotNull Flow.Subscriber<? super R> downstream
    ) {
        downstream.onComplete();
    }

    private class State {
        volatile Flow.Subscriber<? super R> downstream;
        Flow.Subscription deferredSubscription;
        Throwable error;
        boolean completed;
    }
}
