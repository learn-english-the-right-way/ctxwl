package org.zith.expr.ctxwl.core.identity.impl;

import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.UserRepository;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.InterceptedEmailRepositoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

public class InterceptedComponentFactory implements ComponentFactory {
    @Override
    public @NotNull EmailRepositoryImpl createEmailRepositoryImpl(
            Session session,
            MailService mailService,
            UserRepository userRepository
    ) {
        return new InterceptedEmailRepositoryImpl(this, session, mailService, userRepository);
    }
}
