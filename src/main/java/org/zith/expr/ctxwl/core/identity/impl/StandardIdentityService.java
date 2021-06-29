package org.zith.expr.ctxwl.core.identity.impl;

import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.config.PostgreSqlConfiguration;

import java.time.Clock;
import java.util.Random;

public interface StandardIdentityService extends IdentityService {
    static StandardIdentityService create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return IdentityServiceImpl.create(Clock.systemDefaultZone(), new Random(), postgreSqlConfiguration, mailConfiguration);
    }
}
