package org.zith.expr.ctxwl.core.accesscontrol;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.util.Objects;
import java.util.Optional;

class VerifiedCredential {
    private final CredentialManager.ResourceType resourceType;
    private final String identifier;
    private final String applicationKey;
    private final EntailedActiveResourceRole activeResourceRole;
    private final EntailedApplicationKeyRole applicationKeyRole;

    VerifiedCredential(CredentialManager.ResourceType resourceType, String identifier, String applicationKey) {
        Objects.requireNonNull(resourceType);
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(applicationKey);

        this.resourceType = resourceType;
        this.identifier = identifier;
        this.applicationKey = applicationKey;

        this.activeResourceRole = new EntailedActiveResourceRole();
        this.applicationKeyRole = new EntailedApplicationKeyRole();
    }

    public ActiveResourceRole activeResourceRole() {
        return activeResourceRole;
    }

    public ApplicationKeyRole applicationKeyRole() {
        return applicationKeyRole;
    }

    public Subject subject() {
        return SubjectImpl.create(resourceType, identifier);
    }

    private class EntailedActiveResourceRole extends AbstractActiveResourceRole {

        @Override
        public @NotNull CredentialManager.ResourceType resourceType() {
            return resourceType;
        }

        @Override
        public @NotNull String identifier() {
            return identifier;
        }

        @Override
        public Optional<ApplicationKeyRole> optionalApplicationKeyRole() {
            return Optional.of(applicationKeyRole);
        }
    }

    private class EntailedApplicationKeyRole extends AbstractApplicationKeyRole {

        @Override
        public @NotNull ActiveResourceRole activeResourceRole() {
            return activeResourceRole;
        }

        @Override
        public @NotNull String applicationKey() {
            return applicationKey;
        }
    }

}
