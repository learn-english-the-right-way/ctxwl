package org.zith.expr.ctxwl.core.identity.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.zith.expr.ctxwl.core.identity.EmailRegistrationRepository;
import org.zith.expr.ctxwl.core.identity.EmailRepository;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSession;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.CredentialRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration.EmailRegistrationRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

import java.time.Clock;
import java.util.function.Supplier;

public class IdentityServiceSessionImpl implements IdentityServiceSession {

    private final Session session;
    private final Clock clock;
    private final Supplier<EmailRepositoryImpl> emailRepositorySupplier;
    private final Supplier<CredentialRepositoryImpl> credentialRepositorySupplier;
    private final Supplier<EmailRegistrationRepositoryImpl> emailRegistrationRepositorySupplier;

    public IdentityServiceSessionImpl(Session session, MailService mailService) {
        this.session = session;
        clock = Clock.systemDefaultZone();
        emailRepositorySupplier = Suppliers.memoize(() ->
                new EmailRepositoryImpl(session, mailService));
        credentialRepositorySupplier = Suppliers.memoize(() ->
                new CredentialRepositoryImpl(session, clock));
        emailRegistrationRepositorySupplier = Suppliers.memoize(() ->
                new EmailRegistrationRepositoryImpl(
                        session, clock, emailRepositorySupplier.get(), credentialRepositorySupplier.get()));
    }

    public static IdentityServiceSession create(SessionFactory sessionFactory, MailService mailService) {
        Preconditions.checkNotNull(sessionFactory);
        return new IdentityServiceSessionImpl(sessionFactory.openSession(), mailService);
    }

    @Override
    public <T> T withTransaction(Supplier<T> supplier) {
        T value;
        session.getTransaction().begin();
        boolean succeeded = false;
        try {
            value = supplier.get();
            succeeded = true;
        } finally {
            if (!succeeded) {
                session.getTransaction().rollback();
            }
        }
        if (!session.getTransaction().getRollbackOnly()) {
            session.getTransaction().commit();
        }
        return value;
    }

    @Override
    public void close() throws Exception {
        session.close();
    }

    @Override
    public EmailRepository emailRepository() {
        return emailRepositorySupplier.get();
    }

    @Override
    public EmailRegistrationRepository emailRegistrationRepository() {
        return emailRegistrationRepositorySupplier.get();
    }
}
