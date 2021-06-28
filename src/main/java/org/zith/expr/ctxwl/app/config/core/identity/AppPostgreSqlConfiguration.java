package org.zith.expr.ctxwl.app.config.core.identity;

import org.zith.expr.ctxwl.app.config.Configuration;
import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.core.identity.config.PostgreSqlConfiguration;

import java.util.Optional;

public record AppPostgreSqlConfiguration(
        String url,
        String username,
        String password
) implements Configuration<AppPostgreSqlConfiguration> {

    @Override
    public AppPostgreSqlConfiguration merge(AppPostgreSqlConfiguration overriding) {
        return new AppPostgreSqlConfiguration(
                Configurations.overlay(url(), overriding.url()),
                Configurations.overlay(username(), overriding.username()),
                Configurations.overlay(password(), overriding.password())
        );
    }

    public PostgreSqlConfiguration effectiveConfiguration() {
        return new PostgreSqlConfiguration(
                Optional.ofNullable(url()).orElse("jdbc:postgresql://localhost:5432/ctxwl"),
                Optional.ofNullable(username()).orElse("ctxwl"),
                Optional.ofNullable(password()).orElse("ctxwl")
        );
    }

    public static AppPostgreSqlConfiguration empty() {
        return new AppPostgreSqlConfiguration(null, null, null);
    }
}
