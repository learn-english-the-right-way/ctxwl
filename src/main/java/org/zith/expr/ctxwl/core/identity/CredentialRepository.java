package org.zith.expr.ctxwl.core.identity;

public interface CredentialRepository {
    void updateKeys(int offset, String[] keys);

    ControlledResource ensure(ResourceType resourceType, String identifier);

    boolean validatePassword(String password);

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
