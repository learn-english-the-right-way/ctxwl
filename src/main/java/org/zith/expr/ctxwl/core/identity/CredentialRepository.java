package org.zith.expr.ctxwl.core.identity;

public interface CredentialRepository {
    void updateKeys(int offset, String[] keys);

    ControlledResource ensure(CredentialManager.ResourceType resourceType, String identifier);

    boolean validatePassword(String password);

}
