package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import com.google.common.base.Preconditions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.EmailRepository;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

public class EmailRepositoryImpl implements EmailRepository {

    private final Session session;
    private final MailService mailService;

    public EmailRepositoryImpl(Session session, MailService mailService) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(mailService);

        this.session = session;
        this.mailService = mailService;
    }

    public EmailImpl ensure(String address) {
        var cAddress = canonicalizeAddress(address);
        var entity = session.byNaturalId(EmailEntity.class)
                .using("address", cAddress)
                .loadOptional()
                .orElseGet(() -> {
                    var freshEntity = new EmailEntity();
                    freshEntity.setAddress(cAddress);
                    session.persist(freshEntity);
                    return freshEntity;
                });
        return entity.getDelegate().bind(this);
    }

    private String canonicalizeAddress(String address) {
        Preconditions.checkArgument(!address.isEmpty());
        // TODO
        return address;
    }

    public MailService getMailService() {
        return mailService;
    }
}
