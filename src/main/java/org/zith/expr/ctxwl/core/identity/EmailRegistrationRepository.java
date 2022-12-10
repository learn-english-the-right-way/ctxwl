package org.zith.expr.ctxwl.core.identity;

import java.util.List;

public interface EmailRegistrationRepository {
    EmailRegistration register(String address, String password);

    List<EmailRegistration> list(String address);
}
