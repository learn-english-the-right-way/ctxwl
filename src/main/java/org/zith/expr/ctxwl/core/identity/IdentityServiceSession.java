package org.zith.expr.ctxwl.core.identity;

import java.util.function.Supplier;

public interface IdentityServiceSession extends AutoCloseable {
    <T> T withTransaction(Supplier<T> supplier);

    CredentialRepository credentialRepository();

    EmailRepository emailRepository();

    EmailRegistrationRepository emailRegistrationRepository();

    UserRepository userRepository();

    @Override
    void close();
}
