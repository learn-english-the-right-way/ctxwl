package org.zith.expr.ctxwl.app.config.webapi;

import org.zith.expr.ctxwl.app.config.Configuration;
import org.zith.expr.ctxwl.app.config.Configurations;

import java.util.Optional;

public record AppWebApiConfiguration(String baseUri) implements Configuration<AppWebApiConfiguration> {
    public String effectiveBaseUri() {
        return Optional.ofNullable(baseUri).orElse("http://localhost:8888");
    }

    @Override
    public AppWebApiConfiguration merge(AppWebApiConfiguration overriding) {
        return new AppWebApiConfiguration(
                Configurations.overlay(baseUri(), overriding.baseUri())
        );
    }

    public static AppWebApiConfiguration empty() {
        return new AppWebApiConfiguration(null);
    }
}
