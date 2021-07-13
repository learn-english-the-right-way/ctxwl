package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import com.google.common.base.Preconditions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.EmailRepository;
import org.zith.expr.ctxwl.core.identity.UserRepository;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

import java.util.Optional;

public class EmailRepositoryImpl implements EmailRepository {

    private final Session session;
    private final MailService mailService;
    private final UserRepository userRepository;

    public EmailRepositoryImpl(Session session, MailService mailService, UserRepository userRepository) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(mailService);

        this.session = session;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    public EmailImpl ensure(String address) {
        var cAddress = canonicalizeAddress(address);
        var entity = session.byNaturalId(EmailEntity.class)
                .using(EmailEntity_.ADDRESS, cAddress)
                .loadOptional()
                .orElseGet(() -> {
                    var freshEntity = new EmailEntity();
                    freshEntity.setAddress(cAddress);
                    session.persist(freshEntity);
                    return freshEntity;
                });
        return entity.getDelegate().bind(this);
    }

    @Override
    public Optional<Email> get(String address) {
        return get(address, Email.class);
    }

    public <T> Optional<T> get(String address, Class<T> clazz) {
        var cAddress = canonicalizeAddress(address);
        return session.byNaturalId(EmailEntity.class)
                .using(EmailEntity_.ADDRESS, cAddress)
                .loadOptional()
                .map(EmailEntity::getDelegate)
                .map(e -> e.bind(this))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    private String canonicalizeAddress(String address) {
        Preconditions.checkArgument(!address.isEmpty());
        // TODO
        return address;
    }

    MailService getMailService() {
        return mailService;
    }

    UserRepository getUserRepository() {
        return userRepository;
    }
}
