package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import org.zith.expr.ctxwl.core.identity.Email;

public class EmailImpl implements Email {

    private final EmailEntity entity;
    private EmailRepositoryImpl repository;

    public EmailImpl(EmailEntity entity) {
        this.entity = entity;
    }

    public EmailEntity getEntity() {
        return entity;
    }

    @Override
    public String getAddress() {
        return entity.getAddress();
    }

    @Override
    public void sendMessage(Message message) {
        repository.getMailService().sendMessage(getAddress(), message);
    }

    EmailImpl bind(EmailRepositoryImpl emailRepository) {
        this.repository = emailRepository;
        return this;
    }
}
