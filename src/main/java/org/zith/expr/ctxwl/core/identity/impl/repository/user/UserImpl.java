package org.zith.expr.ctxwl.core.identity.impl.repository.user;

import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.UserRepository;

public class UserImpl implements ManagedUser {
    private final UserEntity entity;
    private UserRepositoryImpl repository;
    private ControlledResource controlledResource;

    public UserImpl(UserEntity entity) {
        this.entity = entity;
    }

    @Override
    public ControlledResource getControlledResource() {
        if (controlledResource == null) {
            controlledResource = repository.getCredentialRepository().ensure(
                    ControlledResourceType.USER,
                    entity.getId().toString()
            );
        }
        return controlledResource;
    }

    @Override
    public boolean isManagedBy(UserRepository userRepository) {
        return repository == userRepository;
    }

    @Override
    public UserEntity getEntity() {
        return entity;
    }

    private void initialize() {
        var session = repository.getSession();
        session.persist(entity);
    }

    UserImpl bind(UserRepositoryImpl userRepository) {
        this.repository = userRepository;
        return this;
    }

    static UserImpl create(UserRepositoryImpl userRepository) {
        var user = new UserEntity().getDelegate().bind(userRepository);
        user.initialize();
        return user;
    }
}
