package org.zith.expr.ctxwl.core.accesscontrol;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.util.Optional;
import java.util.function.Predicate;

public interface ActiveResourceRole extends Role {

    @NotNull CredentialManager.ResourceType resourceType();

    @NotNull String identifier();

    Optional<ApplicationKeyRole> optionalApplicationKeyRole();

    @NotNull
    static String name(@NotNull CredentialManager.ResourceType resourceType, @NotNull String identifier) {
        var baseName = baseName(resourceType, identifier);
        return "resource:" + baseName;
    }

    @NotNull
    static String baseName(@NotNull CredentialManager.ResourceType resourceType, @NotNull String identifier) {
        var prefix = switch (resourceType) {
            case USER -> "user";
            case EMAIL_REGISTRATION -> "email-registration";
        };
        return prefix + ":" + AbstractRole.escape(identifier);
    }

    static Predicate<Role> match(CredentialManager.ResourceType resourceType) {
        return role -> {
            if (role instanceof ActiveResourceRole activeResourceRole) {
                return activeResourceRole.resourceType() == resourceType;
            } else {
                return false;
            }
        };
    }

    static Predicate<Role> match(CredentialManager.ResourceType resourceType, String identifier) {
        return role -> {
            if (role instanceof ActiveResourceRole activeResourceRole) {
                return activeResourceRole.resourceType() == resourceType &&
                        activeResourceRole.identifier().equals(identifier);
            } else {
                return false;
            }
        };
    }
}
