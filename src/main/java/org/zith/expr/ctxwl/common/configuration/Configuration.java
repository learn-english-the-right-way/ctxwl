package org.zith.expr.ctxwl.common.configuration;

public interface Configuration<T extends Configuration<T>> {
    T merge(T overriding);
}
