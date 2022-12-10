package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface Realm {
    Optional<CredentialManager.KeyUsage> resolveAuthenticatingKeyUsage(CredentialManager.ResourceType type);

    @NotNull Comparator<ControlledResource> controlledResourceComparator();

    static Builder builder() {
        return new Builder();
    }

    Optional<Principal> authenticate(List<String> applicationKeys);

    @NotThreadSafe
    final class Builder {
        private final ArrayList<CredentialManager.ResourceType> orderedResources;
        private final ImmutableBiMap.Builder<CredentialManager.ResourceType, CredentialManager.KeyUsage>
                resourceTypeKeyUsageMapBuilder;
        private CredentialManager credentialManager;

        private Builder() {
            orderedResources = new ArrayList<>();
            resourceTypeKeyUsageMapBuilder = ImmutableBiMap.builder();
        }

        public Builder authenticationMethod(
                CredentialManager.ResourceType resourceType,
                CredentialManager.KeyUsage keyUsage
        ) {
            orderedResources.add(resourceType);
            resourceTypeKeyUsageMapBuilder.put(resourceType, keyUsage);
            return this;
        }

        public Builder credentialManager(CredentialManager credentialManager) {
            this.credentialManager = credentialManager;
            return this;
        }

        public Realm build() {
            Preconditions.checkNotNull(credentialManager);
            var keyUsagesByResourceTypes = resourceTypeKeyUsageMapBuilder.build();
            var orderedResources = this.orderedResources.stream().toList();
            Preconditions.checkArgument(!keyUsagesByResourceTypes.isEmpty());
            Preconditions.checkArgument(!orderedResources.isEmpty());
            return new RealmImpl(
                    credentialManager,
                    keyUsagesByResourceTypes,
                    orderedResources);
        }
    }
}
