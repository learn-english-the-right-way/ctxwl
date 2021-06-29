package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.time.Instant;
import java.util.Optional;

public interface CredentialSchema {
    byte[] makeSalt(int size);

    Instant timestamp();

    byte[] makeEntropicCode(int size);

    void updateKeys(int offset, String[] keys);

    boolean validatePassword(String password);

    String makeName(CredentialManager.ResourceType resourceType, String identifier);

    ControlledResourceName splitName(String name);

    String keyUsageName(CredentialManager.KeyUsage keyUsage);

    String makeAuthenticationKey(CredentialManager.KeyUsage keyUsage, byte[] code);

    Optional<byte[]> validateAuthenticationKey(CredentialManager.KeyUsage keyUsage, String authenticationKey);

}
