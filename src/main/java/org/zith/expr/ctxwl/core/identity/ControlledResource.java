package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface ControlledResource {
    void setPassword(CredentialRepository.KeyUsage keyUsage, String password);

    String ensureAuthenticationKey(CredentialRepository.KeyUsage keyUsage);

    Optional<String> getAuthenticationKey(CredentialRepository.KeyUsage keyUsage);
}
