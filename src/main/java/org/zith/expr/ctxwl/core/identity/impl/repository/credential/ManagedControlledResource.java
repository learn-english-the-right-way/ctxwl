package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

public interface ManagedControlledResource extends ControlledResource {
    void invalidateKey(CredentialManager.KeyUsage keyUsage);

    ResourceEntity getEntity();
}
