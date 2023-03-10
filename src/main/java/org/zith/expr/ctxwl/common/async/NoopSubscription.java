package org.zith.expr.ctxwl.common.async;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Flow;

final class NoopSubscription implements Flow.Subscription {
    private static volatile NoopSubscription INSTANCE;

    @NotNull
    public static NoopSubscription getInstance() {
        var instance = INSTANCE;
        if (instance == null) {
            synchronized (NoopSubscription.class) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = new NoopSubscription();
                }
            }
        }
        return instance;
    }

    @Override
    public void request(long n) {

    }

    @Override
    public void cancel() {

    }
}
