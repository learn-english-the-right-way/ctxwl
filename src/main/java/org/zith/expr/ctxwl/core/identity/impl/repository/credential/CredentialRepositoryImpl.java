package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;

import java.time.Instant;
import java.util.Objects;
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
    public boolean validatePassword(String password) {
        return credentialSchema.validatePassword(password);
    }

    @Override
    public Optional<ControlledResource> lookupByAuthenticationKeyCode(CredentialManager.Domain domain, byte[] code) {
        var cb = session.getCriteriaBuilder();
        var q = cb.createQuery(ResourceAuthenticationKeyEntity.class);
        var rk = q.from(ResourceAuthenticationKeyEntity.class);
        var rkc = rk.join(ResourceAuthenticationKeyEntity_.effectiveCode);
        q.where(cb.and(
                cb.equal(rkc.get(ResourceAuthenticationKeyCodeEntity_.code), code),
                rk.get(ResourceAuthenticationKeyEntity_.keyUsage)
                        .in(domain.getKeyUsages().stream().map(this::keyUsageName).toList())));
        return session.createQuery(q).uniqueResultOptional()
                .filter(k -> domain.getPrincipalTypes().stream()
                        .anyMatch(t -> Objects.equals(k.getKeyUsage(), keyUsageName(t.authenticationMethod())) &&
                                Objects.equals(k.getResource().getType(), typeName(t.reflectiveType()))))
                .map(ResourceAuthenticationKeyEntity::getResource)
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

    String makeName(CredentialManager.ResourceType resourceType, String identifier) {
        return credentialSchema.makeName(resourceType, identifier);
    }

    String makeAuthenticationKey(CredentialManager.KeyUsage keyUsage, byte[] code) {
        return credentialSchema.makeAuthenticationKey(keyUsage, code);
    }

    String keyUsageName(CredentialManager.KeyUsage keyUsage) {
        return credentialSchema.keyUsageName(keyUsage);
    }

    String typeName(CredentialManager.ResourceType resourceType) {
        return credentialSchema.typeName(resourceType);
    }
}
