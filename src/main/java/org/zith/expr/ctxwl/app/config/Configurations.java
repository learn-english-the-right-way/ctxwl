package org.zith.expr.ctxwl.app.config;

import org.zith.expr.ctxwl.common.configuration.Configuration;

import java.util.Optional;
import java.util.stream.Stream;

public final class Configurations {
    private Configurations() {
    }

    public static <T> T overlay(T base, T overriding) {
        if (overriding != null) {
            return overriding;
        } else {
            return base;
        }
    }

    public static <T extends Configuration<T>> T merge(T base, T overriding) {
        return Stream.of(Optional.ofNullable(base), Optional.ofNullable(overriding))
                .flatMap(Optional::stream)
                .reduce(Configuration::merge)
                .orElse(null);
    }
}
