package org.zith.expr.ctxwl.core.identity.inttest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record IdentityServiceTestConfiguration(
        IdentityServiceTestPostgreSqlConfiguration postgreSql,
        IdentityServiceTestMailConfiguration mail
) implements Configuration<IdentityServiceTestConfiguration> {
    @Override
    public IdentityServiceTestConfiguration merge(IdentityServiceTestConfiguration overriding) {
        return new IdentityServiceTestConfiguration(
                Configurations.merge(postgreSql(), overriding.postgreSql()),
                Configurations.merge(mail(), overriding.mail())
        );
    }

    public static IdentityServiceTestConfiguration empty() {
        return new IdentityServiceTestConfiguration(
                IdentityServiceTestPostgreSqlConfiguration.empty(),
                IdentityServiceTestMailConfiguration.empty()
        );
    }
}
