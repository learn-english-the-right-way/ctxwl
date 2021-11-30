package org.zith.expr.ctxwl.core.identity;

import com.google.common.collect.ImmutableBiMap;

import java.util.Optional;

public interface CredentialManager extends AutoCloseable {
    Optional<ControlledResource> verifyApplicationKeyAndGetResource(
            ImmutableBiMap<ResourceType, KeyUsage> keyUsagesByResourceTypes,
            String applicationKey
    );

    @Override
    void close();

    enum ResourceType {
        USER,
        EMAIL_REGISTRATION,
    }

    enum KeyUsage {
        USER_LOGIN,
        USER_AUTHENTICATION,
        REGISTRATION_CONFIRMATION,
        REGISTRATION_CREDENTIAL_PROPOSAL,
    }

}
