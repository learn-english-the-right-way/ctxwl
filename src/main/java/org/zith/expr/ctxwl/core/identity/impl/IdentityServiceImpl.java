package org.zith.expr.ctxwl.core.identity.impl;

import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSession;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.config.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialmanager.CredentialManagerImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchemaImpl;

import java.time.Clock;
import java.util.Random;

public class IdentityServiceImpl implements StandardIdentityService {
    private final IdentityServiceSessionFactory identityServiceSessionFactory;
    private final CredentialManager credentialManager;
    private final Clock clock;

    private IdentityServiceImpl(
            IdentityServiceSessionFactory identityServiceSessionFactory,
            CredentialManager credentialManager,
            Clock clock
    ) {
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
        identityServiceSessionFactory.close();
        credentialManager.close();
    }

    public static StandardIdentityService create(
            Clock clock,
            Random random,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        var credentialSchema = CredentialSchemaImpl.create(random, clock);
        var identityServiceSessionFactory = IdentityServiceSessionFactoryImpl.create(credentialSchema, clock, postgreSqlConfiguration, mailConfiguration);
        var credentialManager = CredentialManagerImpl.create(credentialSchema, identityServiceSessionFactory);
        return new IdentityServiceImpl(identityServiceSessionFactory, credentialManager, clock);
    }

}
