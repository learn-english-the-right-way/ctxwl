package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;

import java.time.Instant;

public class CredentialRepositoryImpl implements CredentialRepository {
    private final Session session;
    private final CredentialSchema credentialSchema;

    public CredentialRepositoryImpl(Session session, CredentialSchema credentialSchema) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(credentialSchema);
        this.session = session;
        this.credentialSchema = credentialSchema;
    }

    @Override
    public ControlledResource ensure(CredentialManager.ResourceType resourceType, String identifier) {
        var name = credentialSchema.makeName(resourceType, identifier);
        return session
                .byNaturalId(ResourceEntity.class)
                .using("name", name)
                .with(LockOptions.READ)
                .loadOptional()
                .map(ResourceEntity::getDelegate)
                .map(r -> r.bind(this))
                .orElseGet(() -> ControlledResourceImpl.create(this, name));
    }

    Session getSession() {
        return session;
    }

    byte[] makeSalt(int size) {
        return credentialSchema.makeSalt(size);
    }

    Instant timestamp() {
        return credentialSchema.timestamp();
    }

    byte[] makeEntropicCode(int size) {
        return credentialSchema.makeEntropicCode(size);
    }

    @Override
    public void updateKeys(int offset, String[] keys) {
        credentialSchema.updateKeys(offset, keys);
    }

    @Override
    public boolean validatePassword(String password) {
        return credentialSchema.validatePassword(password);
    }

    String makeName(CredentialManager.ResourceType resourceType, String identifier) {
        return credentialSchema.makeName(resourceType, identifier);
    }

    String makeAuthenticationKey(CredentialManager.KeyUsage keyUsage, byte[] code) {
        return credentialSchema.makeAuthenticationKey(keyUsage, code);
    }

    boolean validateAuthenticationKey(CredentialManager.KeyUsage keyUsage, String authenticationKey) {
        return credentialSchema.validateAuthenticationKey(keyUsage, authenticationKey);
    }

    String keyUsageName(CredentialManager.KeyUsage keyUsage) {
        return credentialSchema.keyUsageName(keyUsage);
    }
}
