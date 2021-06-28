package org.zith.expr.ctxwl.core.identity;

public interface EmailRegistrationRepository {
    EmailRegistration register(String address, String password);
}
