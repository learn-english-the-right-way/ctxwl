package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class CredentialRepositoryImpl implements CredentialRepository {
    private final Session session;
    private final CredentialSchema credentialSchema;

    public CredentialRepositoryImpl(ComponentFactory componentFactory, Session session, CredentialSchema credentialSchema) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(credentialSchema);
        this.session = session;
        this.credentialSchema = credentialSchema;
    }

    @Override
    public ControlledResource ensure(ControlledResourceType resourceType, String identifier) {
        return session
                .byNaturalId(ResourceEntity.class)
                .using("name", makeName(resourceType, identifier))
                .with(LockOptions.READ)
                .loadOptional()
                .map(ResourceEntity::getDelegate)
                .map(r -> r.bind(this))
                .orElseGet(() -> ControlledResourceImpl.create(this, resourceType, identifier));
    }

    @Override
    public boolean validateStructureOfPassword(String password) {
        return credentialSchema.validateStructureOfPassword(password);
    }

    @Override
    public Optional<ControlledResource> lookupByApplicationKeyCode(
            ImmutableBiMap<ControlledResourceType, CredentialManager.KeyUsage> keyUsages,
            byte[] code
    ) {
        var cb = session.getCriteriaBuilder();
        var q = cb.createQuery(ResourceApplicationKeyEntity.class);
        var rk = q.from(ResourceApplicationKeyEntity.class);
        var rkc = rk.join(ResourceApplicationKeyEntity_.effectiveCode);
        q.where(cb.and(
                cb.equal(rkc.get(ResourceApplicationKeyCodeEntity_.code), code),
                rk.get(ResourceApplicationKeyEntity_.keyUsage)
                        .in(keyUsages.values().stream().map(this::keyUsageName).toList())));
        return session.createQuery(q).uniqueResultOptional()
                .filter(k -> keyUsages.entrySet().stream()
                        .anyMatch(e -> Objects.equals(k.getKeyUsage(), keyUsageName(e.getValue())) &&
                                Objects.equals(k.getResource().getType(), typeName(e.getKey()))))
                .map(ResourceApplicationKeyEntity::getResource)
                .map(ResourceEntity::getDelegate).map(r -> r.bind(this));
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

    String makeName(ControlledResourceType resourceType, String identifier) {
        return credentialSchema.makeName(resourceType, identifier);
    }

    String makeApplicationKey(CredentialManager.KeyUsage keyUsage, byte[] code) {
        return credentialSchema.makeApplicationKey(keyUsage, code);
    }

    String keyUsageName(CredentialManager.KeyUsage keyUsage) {
        return credentialSchema.keyUsageName(keyUsage);
    }

    String typeName(ControlledResourceType resourceType) {
        return credentialSchema.typeName(resourceType);
    }
}
