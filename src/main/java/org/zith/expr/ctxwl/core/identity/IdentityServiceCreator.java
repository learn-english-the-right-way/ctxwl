package org.zith.expr.ctxwl.core.identity;

import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.identity.impl.DefaultComponentFactory;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Random;

public final class IdentityServiceCreator {
    public static IdentityService create(
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return create(new DefaultComponentFactory(), reinitializeData, postgreSqlConfiguration, mailConfiguration);
    }

    static IdentityService create(
            ComponentFactory componentFactory,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return componentFactory.createIdentityServiceImpl(
                Clock.systemDefaultZone(),
                new SecureRandom(),
                reinitializeData,
                postgreSqlConfiguration,
                mailConfiguration
        );
    }
}
