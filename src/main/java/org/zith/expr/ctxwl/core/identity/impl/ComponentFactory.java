package org.zith.expr.ctxwl.core.identity.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.UserRepository;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.CredentialRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration.EmailRegistrationRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

import java.security.SecureRandom;
import java.time.Clock;

public interface ComponentFactory {
    @NotNull
    default IdentityServiceSessionFactoryImpl createIdentityServiceSessionFactoryImpl(
            CredentialSchema credentialSchema,
            Clock clock,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return IdentityServiceSessionFactoryImpl.create(
                this,
                credentialSchema,
                clock,
                reinitializeData,
                postgreSqlConfiguration,
                mailConfiguration
        );
    }

    @NotNull
    default IdentityServiceImpl createIdentityServiceImpl(
            Clock clock,
            SecureRandom secureRandom,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        return IdentityServiceImpl.create(
                this,
                clock,
                secureRandom,
                reinitializeData,
                postgreSqlConfiguration,
                mailConfiguration
        );
    }

    @NotNull
    default IdentityServiceSessionImpl createIdentityServiceSessionImpl(
            SessionFactory sessionFactory,
            CredentialSchema credentialSchema,
            MailService mailService,
            Clock clock
    ) {
        return IdentityServiceSessionImpl.create(this, sessionFactory, credentialSchema, mailService, clock);
    }

    @NotNull
    default CredentialRepositoryImpl createCredentialRepositoryImpl(
            Session session,
            CredentialSchema credentialSchema
    ) {
        return new CredentialRepositoryImpl(this, session, credentialSchema);
    }

    @NotNull
    default UserRepositoryImpl createUserRepositoryImpl(
            Session session,
            Clock clock,
            CredentialRepository credentialRepository
    ) {
        return new UserRepositoryImpl(this, session, clock, credentialRepository);
    }

    @NotNull
    default EmailRepositoryImpl createEmailRepositoryImpl(
            Session session,
            MailService mailService,
            UserRepository userRepository
    ) {
        return new EmailRepositoryImpl(this, session, mailService, userRepository);
    }

    @NotNull
    default EmailRegistrationRepositoryImpl createEmailRegistrationRepositoryImpl(
            Session session,
            Clock clock,
            EmailRepositoryImpl emailRepository,
            CredentialRepositoryImpl credentialRepository
    ) {
        return new EmailRegistrationRepositoryImpl(
                this,
                session,
                clock,
                emailRepository,
                credentialRepository
        );
    }
}
