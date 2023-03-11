package org.zith.expr.ctxwl.core.identity;

import com.google.common.collect.ImmutableBiMap;

import java.util.Optional;

public interface CredentialRepository {

    ControlledResource ensure(ControlledResourceType resourceType, String identifier);

    boolean validateStructureOfPassword(String password);

    Optional<ControlledResource> lookupByApplicationKeyCode(
            ImmutableBiMap<ControlledResourceType, CredentialManager.KeyUsage> keyUsages,
            byte[] code
    );
}
