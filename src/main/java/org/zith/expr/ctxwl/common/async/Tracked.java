package org.zith.expr.ctxwl.common.async;

import com.google.common.base.Suppliers;

import java.util.function.Function;

public interface Tracked<T> {
    T value();

    void acknowledge();

    static <T> Tracked<T> of(T value, Runnable acknowledge) {
        return new Tracked<>() {
            private volatile boolean acknowledged;

            @Override
            public T value() {
                return value;
            }

            @Override
            public void acknowledge() {
                if (!acknowledged) {
                    acknowledge.run();
                    acknowledged = true;
                }
            }
        };
    }

    default <R> Tracked<R> map(Function<T, R> mapper) {
        var source = this;
        //noinspection Guava
        var valueSupplier = Suppliers.memoize(() -> mapper.apply(value()));
        return new Tracked<>() {
            @Override
            public R value() {
                return valueSupplier.get();
            }

            @Override
            public void acknowledge() {
                source.acknowledge();
            }
        };
    }
}
