package org.zith.expr.ctxwl.core.identity.impl.service.credentialmanager;

import org.zith.expr.ctxwl.core.identity.ControlledResource;
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
    public Optional<ControlledResource> authenticate(Domain domain, String applicationKey) {
        return credentialSchema.validateApplicationKey(domain.getKeyUsages(), applicationKey)
                .flatMap(code -> {
                    try (var session = identityServiceSessionFactory.openSession()) {
                        return session.withTransaction(() ->
                                session.credentialRepository().lookupByApplicationKeyCode(domain, code));
                    }
                });
    }

    @Override
    public Optional<KeyUsage> resolveAuthenticatingKeyUsage(Domain domain, ResourceType type) {
        return domain.getPrincipalTypes().stream()
                .filter(pt -> pt.reflectiveType() == type)
                .findAny()
                .map(PrincipalType::authenticationMethod);
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
