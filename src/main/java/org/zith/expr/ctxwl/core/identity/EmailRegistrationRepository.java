package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface EmailRegistrationRepository {
    EmailRegistration register(String address, String password);

    Optional<EmailRegistration> get(String address);
}
