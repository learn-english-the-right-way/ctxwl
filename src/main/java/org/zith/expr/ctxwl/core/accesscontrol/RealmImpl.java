package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.ControlledResourceUniversalIdentifier;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

final class RealmImpl implements Realm {

    private final CredentialManager credentialManager;
    private final ImmutableBiMap<ControlledResourceType, CredentialManager.KeyUsage> keyUsagesByResourceTypes;
    private final Comparator<ControlledResource> controlledResourceComparator;

    RealmImpl(
            CredentialManager credentialManager,
            ImmutableBiMap<ControlledResourceType, CredentialManager.KeyUsage> keyUsagesByResourceTypes,
            List<ControlledResourceType> orderedResources
    ) {
        this.credentialManager = credentialManager;
        this.keyUsagesByResourceTypes = keyUsagesByResourceTypes;
        //noinspection SuspiciousMethodCalls
        this.controlledResourceComparator = Comparator.comparing(orderedResources::indexOf);
    }

    @Override
    public Optional<CredentialManager.KeyUsage> resolveAuthenticatingKeyUsage(ControlledResourceType type) {
        return Optional.ofNullable(keyUsagesByResourceTypes.get(type));
    }

    @Override
    @NotNull
    public Comparator<ControlledResource> controlledResourceComparator() {
        return controlledResourceComparator;
    }

    @Override
    public List<Principal> authenticate(List<String> applicationKeys) {
        record VerifiedCredential(ControlledResourceUniversalIdentifier resource, String applicationKey) {
        }
        return applicationKeys.stream()
                .map(key -> credentialManager
                        .verifyApplicationKeyAndGetResource(keyUsagesByResourceTypes, key)
                        .map(r -> new VerifiedCredential(r.getUniversalIdentifier(), key)))
                .flatMap(Optional::stream)
                .collect(Collector.of(
                        () -> new LinkedHashMap<ControlledResourceUniversalIdentifier, Set<String>>(),
                        (map, credential) -> {
                            var keys = map.computeIfAbsent(credential.resource(), (k) -> new HashSet<>());
                            keys.add(credential.applicationKey());
                        },
                        (a, b) -> {
                            var result = new LinkedHashMap<ControlledResourceUniversalIdentifier, Set<String>>();
                            Stream.of(a, b).forEachOrdered((s) ->
                                    b.forEach((resource, keys) ->
                                            result.computeIfAbsent(resource, (k) -> new HashSet<>())
                                                    .addAll(keys)));
                            return result;
                        },
                        m -> {
                            var result = new ArrayList<Principal>(m.size());
                            m.forEach((resource, keys) -> result.add(PrincipalImpl.create(this, resource, keys)));
                            return result;
                        }));

    }

}
