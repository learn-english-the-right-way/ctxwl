package org.zith.expr.ctxwl.core.identity;

public interface CredentialManager {
    enum ResourceType {
        EMAIL_REGISTRATION,
    }

    enum KeyUsage {
        REGISTRATION,
        REGISTRATION_CONFIRMATION,
        USER_LOGIN,
        USER_AUTHENTICATION,
    }
}
