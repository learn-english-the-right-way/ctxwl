package org.zith.expr.ctxwl.core.identity.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zith.expr.ctxwl.core.identity.*;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.CredentialRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration.EmailRegistrationRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

import javax.persistence.OptimisticLockException;
import java.time.Clock;
import java.util.function.Supplier;

public class IdentityServiceSessionImpl implements IdentityServiceSession {
    private static final Logger logger = LoggerFactory.getLogger(IdentityServiceSessionImpl.class);

    private final ComponentFactory componentFactory;

    private final Session session;
    private final Supplier<CredentialRepositoryImpl> credentialRepositorySupplier;
    private final Supplier<UserRepositoryImpl> userRepositorySupplier;
    private final Supplier<EmailRepositoryImpl> emailRepositorySupplier;
    private final Supplier<EmailRegistrationRepositoryImpl> emailRegistrationRepositorySupplier;

    public IdentityServiceSessionImpl(
            ComponentFactory componentFactory,
            Session session,
            CredentialSchema credentialSchema,
            MailService mailService,
            Clock clock
    ) {
        this.componentFactory = componentFactory;
        this.session = session;
        credentialRepositorySupplier = Suppliers.memoize(() ->
                createCredentialRepository(session, credentialSchema));
        userRepositorySupplier = Suppliers.memoize(() ->
                createUserRepository(session, clock, credentialRepositorySupplier.get()));
        emailRepositorySupplier = Suppliers.memoize(() ->
                createEmailRepository(session, mailService, userRepositorySupplier.get()));
        emailRegistrationRepositorySupplier = Suppliers.memoize(() ->
                createEmailRegistrationRepository(session, clock, emailRepositorySupplier.get(), credentialRepositorySupplier.get()));
    }

    @NotNull
    private CredentialRepositoryImpl createCredentialRepository(Session session, CredentialSchema credentialSchema) {
        return componentFactory.createCredentialRepositoryImpl(session, credentialSchema);
    }

    @NotNull
    private UserRepositoryImpl createUserRepository(
            Session session,
            Clock clock,
            CredentialRepositoryImpl credentialRepository
    ) {
        return componentFactory.createUserRepositoryImpl(session, clock, credentialRepository);
    }

    @NotNull
    private EmailRepositoryImpl createEmailRepository(
            Session session,
            MailService mailService,
            UserRepositoryImpl userRepository
    ) {
        return componentFactory.createEmailRepositoryImpl(session, mailService, userRepository);
    }

    @NotNull
    private EmailRegistrationRepositoryImpl createEmailRegistrationRepository(
            Session session,
            Clock clock,
            EmailRepositoryImpl emailRepository,
            CredentialRepositoryImpl credentialRepository
    ) {
        return componentFactory.createEmailRegistrationRepositoryImpl(
                session, clock, emailRepository, credentialRepository);
    }

    public static IdentityServiceSessionImpl create(
            ComponentFactory componentFactory,
            SessionFactory sessionFactory,
            CredentialSchema credentialSchema,
            MailService mailService,
            Clock clock
    ) {
        Preconditions.checkNotNull(sessionFactory);
        return new IdentityServiceSessionImpl(
                componentFactory,
                sessionFactory.openSession(),
                credentialSchema,
                mailService,
                clock
        );
    }

    @Override
    public <T> T withTransaction(Supplier<T> supplier) {
        for (int attempt = 0; ; ++attempt) {
            try {
                T value;
                var transaction = session.getTransaction();
                transaction.begin();
                boolean succeeded = false;
                try {
                    value = supplier.get();
                    if (!transaction.getRollbackOnly()) {
                        transaction.commit();
                        succeeded = true;
                    }
                } finally {
                    if (!succeeded && transaction.isActive()) {
                        transaction.rollback();
                    }
                }
                return value;
            } catch (OptimisticLockException e) {
                if (attempt < 5) {
                    logger.info("Retrying transaction");
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public CredentialRepository credentialRepository() {
        return credentialRepositorySupplier.get();
    }

    @Override
    public EmailRepository emailRepository() {
        return emailRepositorySupplier.get();
    }

    @Override
    public EmailRegistrationRepository emailRegistrationRepository() {
        return emailRegistrationRepositorySupplier.get();
    }

    @Override
    public UserRepository userRepository() {
        return userRepositorySupplier.get();
    }
}
