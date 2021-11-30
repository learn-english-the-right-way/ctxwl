package org.zith.expr.ctxwl.core.identity;

import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.identity.impl.DefaultComponentFactory;

import java.time.Clock;
import java.util.Random;

public final class IdentityServiceCreator {
    public static IdentityService create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return create(new DefaultComponentFactory(), postgreSqlConfiguration, mailConfiguration);
    }

    static IdentityService create(
            ComponentFactory componentFactory, PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return componentFactory.createIdentityServiceImpl(
                Clock.systemDefaultZone(),
                new Random(),
                postgreSqlConfiguration,
                mailConfiguration
        );
    }
}
