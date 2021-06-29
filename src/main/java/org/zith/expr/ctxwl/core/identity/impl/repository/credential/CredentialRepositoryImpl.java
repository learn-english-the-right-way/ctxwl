package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.ControlledResourceName;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;

import java.time.Instant;
import java.util.Optional;

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
        var name = makeName(resourceType, identifier);
        return session
                .byNaturalId(ResourceEntity.class)
                .using("name", name)
                .with(LockOptions.READ)
                .loadOptional()
                .map(ResourceEntity::getDelegate)
                .map(r -> r.bind(this))
                .orElseGet(() -> ControlledResourceImpl.create(this, name));
    }

    @Override
    public boolean validatePassword(String password) {
        return credentialSchema.validatePassword(password);
    }

    @Override
    public Optional<ControlledResource> lookupByAuthenticationKeyCode(CredentialManager.KeyUsage keyUsage, byte[] code) {
        var cb = session.getCriteriaBuilder();
        var q = cb.createQuery(ResourceEntity.class);
        var r = q.from(ResourceEntity.class);
        var rk = r.join(ResourceEntity_.authenticationKeys);
        q.where(cb.and(
                cb.equal(rk.get(ResourceAuthenticationKeyEntity_.effectiveCode), code)),
                cb.equal(rk.get(ResourceAuthenticationKeyEntity_.keyUsage), keyUsageName(keyUsage)));
        return session.createQuery(q).uniqueResultOptional().map(ResourceEntity::getDelegate).map(d -> d.bind(this));
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


    String makeName(CredentialManager.ResourceType resourceType, String identifier) {
        return credentialSchema.makeName(resourceType, identifier);
    }

    ControlledResourceName splitName(String name) {
        return credentialSchema.splitName(name);
    }

    String makeAuthenticationKey(CredentialManager.KeyUsage keyUsage, byte[] code) {
        return credentialSchema.makeAuthenticationKey(keyUsage, code);
    }

    String keyUsageName(CredentialManager.KeyUsage keyUsage) {
        return credentialSchema.keyUsageName(keyUsage);
    }
}
