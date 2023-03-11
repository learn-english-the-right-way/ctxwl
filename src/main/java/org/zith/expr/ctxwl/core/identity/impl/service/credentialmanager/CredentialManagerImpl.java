package org.zith.expr.ctxwl.core.identity.impl.service.credentialmanager;

import com.google.common.collect.ImmutableBiMap;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.impl.IdentityServiceSessionFactoryImpl;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;

import java.util.Objects;
import java.util.Optional;

public class CredentialManagerImpl implements CredentialManager {
    private final IdentityServiceSessionFactory identityServiceSessionFactory;
    private final CredentialSchema credentialSchema;

    private CredentialManagerImpl(
            IdentityServiceSessionFactory identityServiceSessionFactory,
            CredentialSchema credentialSchema
    ) {
        Objects.requireNonNull(identityServiceSessionFactory);
        Objects.requireNonNull(credentialSchema);
        this.identityServiceSessionFactory = identityServiceSessionFactory;
        this.credentialSchema = credentialSchema;
    }

    @Override
    public Optional<ControlledResource> verifyApplicationKeyAndGetResource(
            ImmutableBiMap<ControlledResourceType, KeyUsage> keyUsagesByResourceTypes,
            String applicationKey
    ) {
        return credentialSchema.validateApplicationKey(keyUsagesByResourceTypes.values(), applicationKey)
                .flatMap(code -> {
                    try (var session = identityServiceSessionFactory.openSession()) {
                        return session.withTransaction(() ->
                                session.credentialRepository()
                                        .lookupByApplicationKeyCode(keyUsagesByResourceTypes, code));
                    }
                });
    }

    @Override
    public void close() {

    }

    public static CredentialManagerImpl create(
            CredentialSchema credentialSchema,
            IdentityServiceSessionFactoryImpl identityServiceSessionFactory
    ) {
        return new CredentialManagerImpl(identityServiceSessionFactory, credentialSchema);
    }
}
