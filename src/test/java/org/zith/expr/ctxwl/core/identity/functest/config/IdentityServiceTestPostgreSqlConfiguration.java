package org.zith.expr.ctxwl.core.identity.functest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;

import java.util.Optional;

public record IdentityServiceTestPostgreSqlConfiguration(
        String uri,
        String username,
        String password
) implements Configuration<IdentityServiceTestPostgreSqlConfiguration> {

    @Override
    public IdentityServiceTestPostgreSqlConfiguration merge(IdentityServiceTestPostgreSqlConfiguration overriding) {
        return new IdentityServiceTestPostgreSqlConfiguration(
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

    public static IdentityServiceTestPostgreSqlConfiguration empty() {
        return new IdentityServiceTestPostgreSqlConfiguration(null, null, null);
    }
}
