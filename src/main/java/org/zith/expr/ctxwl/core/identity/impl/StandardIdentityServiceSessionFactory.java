package org.zith.expr.ctxwl.core.identity.impl;

import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.config.PostgreSqlConfiguration;

public interface StandardIdentityServiceSessionFactory extends IdentityServiceSessionFactory {
    static StandardIdentityServiceSessionFactory create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return IdentityServiceSessionFactoryImpl.create(postgreSqlConfiguration, mailConfiguration);
    }
}
