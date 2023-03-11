package org.zith.expr.ctxwl.core.identity;

import com.google.common.collect.ImmutableBiMap;

import java.util.Optional;

public interface CredentialManager extends AutoCloseable {
    Optional<ControlledResource> verifyApplicationKeyAndGetResource(
            ImmutableBiMap<ControlledResourceType, KeyUsage> keyUsagesByResourceTypes,
            String applicationKey
    );

    @Override
    void close();

    enum KeyUsage {
        USER_LOGIN,
        USER_AUTHENTICATION,
        REGISTRATION_CONFIRMATION,
        REGISTRATION_CREDENTIAL_PROPOSAL,
    }

}
