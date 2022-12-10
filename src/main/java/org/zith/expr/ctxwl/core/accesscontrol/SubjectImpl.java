package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Suppliers;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.util.Objects;
import java.util.function.Supplier;

class SubjectImpl implements Subject {

    private final SubjectDefinition definition;
    private final Supplier<String> nameSupplier;

    private SubjectImpl(SubjectDefinition definition) {
        this.definition = definition;
        this.nameSupplier = Suppliers.memoize(() ->
                ActiveResourceRole.baseName(this.definition.resourceType(), this.definition.identifier()));
    }

    @Override
    public String name() {
        return nameSupplier.get();
    }

    public static SubjectImpl create(CredentialManager.ResourceType resourceType, String identifier) {
        return new SubjectImpl(new SubjectDefinition(resourceType, identifier));
    }

    private record SubjectDefinition(CredentialManager.ResourceType resourceType, String identifier) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectImpl subject = (SubjectImpl) o;
        return definition.equals(subject.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition);
    }

    @Override
    public String toString() {
        return "SubjectImpl{" +
                "definition=" + definition +
                '}';
    }
}
