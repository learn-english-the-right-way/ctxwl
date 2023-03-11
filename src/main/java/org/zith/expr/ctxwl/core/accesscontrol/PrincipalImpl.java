package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.net.UrlEscapers;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.ControlledResourceUniversalIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

class PrincipalImpl implements Principal {
    private final Realm realm;
    private final ControlledResourceUniversalIdentifier resource;
    private final Set<String> applicationKeys;
    private final Supplier<String> nameSupplier;

    private PrincipalImpl(RealmImpl realm, ControlledResourceUniversalIdentifier resource, Set<String> applicationKeys) {
        this.realm = realm;
        this.resource = resource;
        this.applicationKeys = applicationKeys;
        this.nameSupplier = Suppliers.memoize(() -> baseName(this.resource.type(), this.resource.identifier()));
    }

    @NotNull
    static PrincipalImpl create(RealmImpl realm, ControlledResourceUniversalIdentifier resource, Set<String> credential) {
        Preconditions.checkNotNull(realm);
        Preconditions.checkNotNull(resource);
        Preconditions.checkNotNull(resource.type());
        Preconditions.checkNotNull(resource.identifier());
        return new PrincipalImpl(realm, resource, Collections.unmodifiableSet(new HashSet<>(credential)));
    }

    @Override
    public Realm realm() {
        return realm;
    }

    @Override
    public String name() {
        return nameSupplier.get();
    }

    @Override
    public ControlledResourceUniversalIdentifier resourceIdentifier() {
        return resource;
    }

    @Override
    public Set<String> applicationKeys() {
        return applicationKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrincipalImpl principal = (PrincipalImpl) o;
        return realm.equals(principal.realm) && resource.equals(principal.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realm, resource);
    }

    @NotNull
    static String baseName(@NotNull ControlledResourceType resourceType, @NotNull String identifier) {
        var prefix = switch (resourceType) {
            case USER -> "user";
            case EMAIL_REGISTRATION -> "email-registration";
        };
        return prefix + ":" + UrlEscapers.urlFormParameterEscaper().escape(identifier);
    }
}
