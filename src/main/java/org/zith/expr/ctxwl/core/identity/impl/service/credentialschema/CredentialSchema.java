package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.time.Instant;

public interface CredentialSchema {
    byte[] makeSalt(int size);

    Instant timestamp();

    byte[] makeEntropicCode(int size);

    void updateKeys(int offset, String[] keys);

    boolean validatePassword(String password);

    String makeName(CredentialManager.ResourceType resourceType, String identifier);

    String keyUsageName(CredentialManager.KeyUsage keyUsage);

    String makeAuthenticationKey(CredentialManager.KeyUsage keyUsage, byte[] code);

    boolean validateAuthenticationKey(CredentialManager.KeyUsage keyUsage, String authenticationKey);
}
