package org.zith.expr.ctxwl.core.identity.impl;

import org.zith.expr.ctxwl.common.close.CombinedAutoCloseable;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSession;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialmanager.CredentialManagerImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchemaImpl;

import java.security.SecureRandom;
import java.time.Clock;

public class IdentityServiceImpl implements IdentityService {
    private final CombinedAutoCloseable closeable;
    private final IdentityServiceSessionFactory identityServiceSessionFactory;
    private final CredentialManager credentialManager;
    private final Clock clock;

    private IdentityServiceImpl(
            CombinedAutoCloseable closeable,
            IdentityServiceSessionFactory identityServiceSessionFactory,
            CredentialManager credentialManager,
            Clock clock
    ) {
        this.closeable = closeable;
        this.identityServiceSessionFactory = identityServiceSessionFactory;
        this.credentialManager = credentialManager;
        this.clock = clock;
    }

    @Override
    public CredentialManager credentialManager() {
        return credentialManager;
    }

    @Override
    public IdentityServiceSession openSession() {
        return identityServiceSessionFactory.openSession();
    }

    @Override
    public void close() {
        closeable.close();
    }

    public static IdentityServiceImpl create(
            ComponentFactory componentFactory, Clock clock,
            SecureRandom secureRandom,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        try (var closeable = CombinedAutoCloseable.create()) {
            var credentialSchema = CredentialSchemaImpl.create(secureRandom, clock);
            var identityServiceSessionFactory = closeable.register(
                    componentFactory.createIdentityServiceSessionFactoryImpl(
                            credentialSchema,
                            clock,
                            reinitializeData,
                            postgreSqlConfiguration,
                            mailConfiguration
                    ));
            var credentialManager = closeable.register(
                    CredentialManagerImpl.create(credentialSchema, identityServiceSessionFactory));
            return new IdentityServiceImpl(
                    closeable.transfer(), identityServiceSessionFactory, credentialManager, clock);
        }
    }

}
