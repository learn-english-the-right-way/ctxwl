package org.zith.expr.ctxwl.core.identity.impl.repository.user;

import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.User;
import org.zith.expr.ctxwl.core.identity.UserRepository;

import java.time.Clock;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final Session session;
    private final Clock clock;
    private final CredentialRepository credentialRepository;

    public UserRepositoryImpl(Session session, Clock clock, CredentialRepository credentialRepository) {
        this.session = session;
        this.clock = clock;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public User register() {
        var user = UserImpl.create(this);
        user.getControlledResource().ensureAuthenticationKey(CredentialManager.KeyUsage.USER_AUTHENTICATION);
        return user;
    }

    @Override
    public Optional<User> get(long id) {
        return Optional.ofNullable(session.get(UserEntity.class, id))
                .map(UserEntity::getDelegate)
                .map(u -> u.bind(this));
    }

    Session getSession() {
        return session;
    }

    CredentialRepository getCredentialRepository() {
        return credentialRepository;
    }
}
