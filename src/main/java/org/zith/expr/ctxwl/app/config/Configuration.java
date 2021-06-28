package org.zith.expr.ctxwl.app.config;

public interface Configuration<T extends Configuration<T>> {
    T merge(T overriding);
}
