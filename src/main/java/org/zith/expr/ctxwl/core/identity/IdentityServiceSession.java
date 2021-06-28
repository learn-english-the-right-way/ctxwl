package org.zith.expr.ctxwl.core.identity;

import java.util.function.Supplier;

public interface IdentityServiceSession extends AutoCloseable {
    <T> T withTransaction(Supplier<T> supplier);

    EmailRepository emailRepository();
    EmailRegistrationRepository emailRegistrationRepository();
}
