package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.User;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.ManagedUser;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserEntity;

import java.util.Optional;

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

    @Override
    public void link(User user) {
        if (entity.getUser() != null) {
            throw new IllegalArgumentException();
        }

        if (user instanceof ManagedUser managedUser) {
            if (!managedUser.isManagedBy(repository.getUserRepository())) {
                throw new IllegalArgumentException("This email cannot be linked to a user from another repository");
            }

            entity.setUser(managedUser.getEntity());
        } else {
            throw new IllegalArgumentException("This email cannot be linked to a foreign user");
        }
    }

    @Override
    public Optional<User> getUser() {
        return Optional.ofNullable(entity.getUser())
                .map(UserEntity::getId)
                .flatMap(repository.getUserRepository()::get);
    }

    EmailImpl bind(EmailRepositoryImpl emailRepository) {
        this.repository = emailRepository;
        return this;
    }
}
