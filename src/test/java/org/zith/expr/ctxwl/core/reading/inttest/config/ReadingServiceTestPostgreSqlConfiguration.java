package org.zith.expr.ctxwl.core.reading.inttest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;

import java.util.Optional;

public record ReadingServiceTestPostgreSqlConfiguration(
        String uri,
        String username,
        String password
) implements Configuration<ReadingServiceTestPostgreSqlConfiguration> {

    @Override
    public ReadingServiceTestPostgreSqlConfiguration merge(ReadingServiceTestPostgreSqlConfiguration overriding) {
        return new ReadingServiceTestPostgreSqlConfiguration(
                Configurations.overlay(uri(), overriding.uri()),
                Configurations.overlay(username(), overriding.username()),
                Configurations.overlay(password(), overriding.password())
        );
    }

    public PostgreSqlConfiguration effectiveConfiguration() {
        return new PostgreSqlConfiguration(
                Optional.ofNullable(uri()).orElse("jdbc:postgresql://localhost:5432/ctxwl"),
                Optional.ofNullable(username()).orElse("ctxwl"),
                Optional.ofNullable(password()).orElse("ctxwl")
        );
    }

    public static ReadingServiceTestPostgreSqlConfiguration empty() {
        return new ReadingServiceTestPostgreSqlConfiguration(null, null, null);
    }
}
