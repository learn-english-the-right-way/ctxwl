package org.zith.expr.ctxwl.core.identity;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.InterceptedComponentFactory;

public class InterceptedIdentityServiceCreator {
    @NotNull
    public static IdentityService create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return IdentityServiceCreator.create(
                new InterceptedComponentFactory(),
                true,
                postgreSqlConfiguration,
                mailConfiguration);
    }
}
