package org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration;

import com.google.common.base.Preconditions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.EmailRegistration;
import org.zith.expr.ctxwl.core.identity.EmailRegistrationRepository;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.CredentialRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryImpl;

import java.time.Clock;
import java.util.Random;

public class EmailRegistrationRepositoryImpl implements EmailRegistrationRepository {
    private final Session session;
    private final Clock clock;
    private final EmailRepositoryImpl emailRepository;
    private final CredentialRepositoryImpl credentialRepository;
    private final Random random;

    public EmailRegistrationRepositoryImpl(
            Session session,
            Clock clock,
            EmailRepositoryImpl emailRepository,
            CredentialRepositoryImpl credentialRepository
    ) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(clock);
        Preconditions.checkNotNull(emailRepository);
        Preconditions.checkNotNull(credentialRepository);

        this.session = session;
        this.clock = clock;
        this.emailRepository = emailRepository;
        this.credentialRepository = credentialRepository;
        random = new Random();
    }

    @Override
    public EmailRegistration register(String address, String password) {
        Preconditions.checkArgument(credentialRepository.validatePassword(password));

        var email = emailRepository.ensure(address);
        return EmailRegistrationImpl.create(this, email, password, makeConfirmationCode(), clock.instant());
    }

    private String makeConfirmationCode() {
        var sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CONFIRMATION_CODE_CHARSET.charAt(random.nextInt(CONFIRMATION_CODE_CHARSET.length())));
        }
        return sb.toString();
    }

    private static final String CONFIRMATION_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    Session getSession() {
        return session;
    }

    EmailRepositoryImpl getEmailRepository() {
        return emailRepository;
    }

    CredentialRepository getCredentialRepository() {
        return credentialRepository;
    }

}
