package org.zith.expr.ctxwl.webapi.access;

import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class RealmImpl implements Realm {

    private final CredentialManager credentialManager;
    private final ImmutableBiMap<CredentialManager.ResourceType, CredentialManager.KeyUsage> keyUsagesByResourceTypes;
    private final Comparator<ControlledResource> controlledResourceComparator;

    RealmImpl(
            CredentialManager credentialManager,
            ImmutableBiMap<CredentialManager.ResourceType, CredentialManager.KeyUsage> keyUsagesByResourceTypes,
            List<CredentialManager.ResourceType> orderedResources
    ) {
        this.credentialManager = credentialManager;
        this.keyUsagesByResourceTypes = keyUsagesByResourceTypes;
        //noinspection SuspiciousMethodCalls
        this.controlledResourceComparator = Comparator.comparing(orderedResources::indexOf);
    }

    @Override
    public Optional<CredentialManager.KeyUsage> resolveAuthenticatingKeyUsage(CredentialManager.ResourceType type) {
        return Optional.ofNullable(keyUsagesByResourceTypes.get(type));
    }

    @Override
    @NotNull
    public Comparator<ControlledResource> controlledResourceComparator() {
        return controlledResourceComparator;
    }

    @Override
    public Optional<Principal> authenticate(List<String> applicationKeys) {
        return Optional.of(
                        applicationKeys.stream()
                                .flatMap(key ->
                                        credentialManager
                                                .verifyApplicationKeyAndGetResource(keyUsagesByResourceTypes, key)
                                                .map(r -> new VerifiedCredential(r.getType(), r.getIdentifier(), key))
                                                .stream())
                                .toList())
                .filter(c -> !c.isEmpty())
                .map(c -> PrincipalImpl.create(this, c));
    }

}
