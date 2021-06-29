package org.zith.expr.ctxwl.core.identity;

import java.util.Optional;

public interface CredentialManager extends AutoCloseable {
    Optional<String> authenticate(PrincipalType principalType, String authenticationKey);

    @Override
    void close();

    enum ResourceType {
        EMAIL_REGISTRATION,
    }

    enum KeyUsage {
        REGISTRATION_CONFIRMATION,
        REGISTRATION_CREDENTIAL_PROPOSAL,
        USER_LOGIN,
        USER_AUTHENTICATION,
    }

    enum PrincipalType {
        EMAIL_REGISTRANT(ResourceType.EMAIL_REGISTRATION, KeyUsage.REGISTRATION_CONFIRMATION);

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
}
