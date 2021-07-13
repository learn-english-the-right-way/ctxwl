package org.zith.expr.ctxwl.core.identity.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

    private final Session session;
    private final Supplier<CredentialRepositoryImpl> credentialRepositorySupplier;
    private final Supplier<UserRepositoryImpl> userRepositorySupplier;
    private final Supplier<EmailRepositoryImpl> emailRepositorySupplier;
    private final Supplier<EmailRegistrationRepositoryImpl> emailRegistrationRepositorySupplier;

    public IdentityServiceSessionImpl(Session session, CredentialSchema credentialSchema, MailService mailService, Clock clock) {
        this.session = session;
        credentialRepositorySupplier = Suppliers.memoize(() ->
                new CredentialRepositoryImpl(session, credentialSchema));
        userRepositorySupplier = Suppliers.memoize(() ->
                new UserRepositoryImpl(session, clock, credentialRepositorySupplier.get()));
        emailRepositorySupplier = Suppliers.memoize(() ->
                new EmailRepositoryImpl(session, mailService, userRepositorySupplier.get()));
        emailRegistrationRepositorySupplier = Suppliers.memoize(() ->
                new EmailRegistrationRepositoryImpl(
                        session, clock, emailRepositorySupplier.get(), credentialRepositorySupplier.get()));
    }

    public static IdentityServiceSession create(SessionFactory sessionFactory, CredentialSchema credentialSchema, MailService mailService, Clock clock) {
        Preconditions.checkNotNull(sessionFactory);
        return new IdentityServiceSessionImpl(sessionFactory.openSession(), credentialSchema, mailService, clock);
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
                    }
                    succeeded = true;
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
