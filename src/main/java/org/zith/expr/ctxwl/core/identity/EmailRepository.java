package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface EmailRepository {
    Email ensure(String address);

    Optional<Email> get(String address);
}
