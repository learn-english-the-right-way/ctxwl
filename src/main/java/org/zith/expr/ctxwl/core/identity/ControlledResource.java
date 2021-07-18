package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface ControlledResource {
    CredentialManager.ResourceType getType();

    String getIdentifier();

    boolean validatePassword(CredentialManager.KeyUsage keyUsage, String password);

    void setPassword(CredentialManager.KeyUsage keyUsage, String password);

    String ensureAuthenticationKey(CredentialManager.KeyUsage keyUsage);

    Optional<byte[]> getAuthenticationKeyCode(CredentialManager.KeyUsage keyUsage);

    Optional<String> getAuthenticationKey(CredentialManager.KeyUsage keyUsage);

    void importKey(
            ControlledResource source,
            CredentialManager.KeyUsage sourceKeyUsage,
            CredentialManager.KeyUsage targetKeyUsage,
            boolean migrate
    );
}
