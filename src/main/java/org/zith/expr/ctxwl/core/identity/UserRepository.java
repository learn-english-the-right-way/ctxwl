package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface UserRepository {
    User register();

    Optional<User> get(long id);
}
