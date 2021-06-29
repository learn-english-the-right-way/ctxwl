package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface ControlledResource {
    void setPassword(CredentialManager.KeyUsage keyUsage, String password);

    String ensureAuthenticationKey(CredentialManager.KeyUsage keyUsage);

    Optional<String> getAuthenticationKey(CredentialManager.KeyUsage keyUsage);
}
