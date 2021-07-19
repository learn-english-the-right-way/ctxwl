package org.zith.expr.ctxwl.core.identity;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface CredentialManager extends AutoCloseable {
    Optional<ControlledResource> authenticate(Domain domain, String applicationKey);

    Optional<KeyUsage> resolveAuthenticatingKeyUsage(Domain domain, ResourceType type);

    @Override
    void close();

    enum ResourceType {
        USER,
        EMAIL_REGISTRATION,
    }

    enum KeyUsage {
        USER_LOGIN,
        USER_AUTHENTICATION,
        REGISTRATION_CONFIRMATION,
        REGISTRATION_CREDENTIAL_PROPOSAL,
    }

    enum PrincipalType {
        USER(ResourceType.USER, KeyUsage.USER_AUTHENTICATION),
        EMAIL_REGISTRANT(ResourceType.EMAIL_REGISTRATION, KeyUsage.REGISTRATION_CONFIRMATION),
        ;

        private final ResourceType reflectiveType;
        private final KeyUsage authenticationMethod;

        PrincipalType(ResourceType reflectiveType, KeyUsage authenticationMethod) {
            this.reflectiveType = reflectiveType;
            this.authenticationMethod = authenticationMethod;
        }

        public ResourceType reflectiveType() {
            return reflectiveType;
        }

        public KeyUsage authenticationMethod() {
            return authenticationMethod;
        }
    }

    enum Domain {
        GENERAL_ACCESS(PrincipalType.USER, PrincipalType.EMAIL_REGISTRANT),
        ;

        private final List<PrincipalType> principalTypes;
        private final Set<KeyUsage> keyUsages;

        Domain(PrincipalType... principalTypes) {
            Arrays.stream(principalTypes)
                    .collect(Collectors.groupingBy(PrincipalType::authenticationMethod))
                    .forEach((key, value) -> Preconditions.checkArgument(
                            value.size() <= 1,
                            "Multiple resources are requested to be authenticated through the same key usage." +
                                    " Only one key usage is allowed at most." +
                                    " - key usage: %s, resources %s", key, value));
            this.principalTypes = List.of(principalTypes);
            keyUsages = this.principalTypes.stream()
                    .map(PrincipalType::authenticationMethod)
                    .collect(Collectors.toUnmodifiableSet());
        }

        public List<PrincipalType> getPrincipalTypes() {
            return principalTypes;
        }

        public Set<KeyUsage> getKeyUsages() {
            return keyUsages;
        }
    }
}
