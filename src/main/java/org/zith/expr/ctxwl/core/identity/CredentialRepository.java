package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface CredentialRepository {

    ControlledResource ensure(CredentialManager.ResourceType resourceType, String identifier);

    boolean validatePassword(String password);

    Optional<ControlledResource> lookupByAuthenticationKeyCode(CredentialManager.Domain domain, byte[] code);
}
