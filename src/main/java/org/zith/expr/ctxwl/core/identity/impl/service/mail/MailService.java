package org.zith.expr.ctxwl.core.identity.impl.service.mail;

import org.zith.expr.ctxwl.core.identity.Email;

public interface MailService {
    void sendMessage(String to, Email.Message message);
}
