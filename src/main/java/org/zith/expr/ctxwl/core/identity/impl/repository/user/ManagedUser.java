package org.zith.expr.ctxwl.core.identity.impl.repository.user;

import org.zith.expr.ctxwl.core.identity.User;
import org.zith.expr.ctxwl.core.identity.UserRepository;

public interface ManagedUser extends User {
    boolean isManagedBy(UserRepository userRepository);

    UserEntity getEntity();
}
