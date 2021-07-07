package org.zith.expr.ctxwl.webapi.authentication;

import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.security.Principal;
import java.util.Objects;
import java.util.stream.Stream;

public class SimplePrincipal implements Principal {

    private final CredentialManager.ResourceType type;
    private final String identifier;

    SimplePrincipal(CredentialManager.ResourceType type, String identifier) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(identifier);

        this.type = type;
        this.identifier = identifier;
    }

    @Override
    public String getName() {
        var prefix = switch (type) {
            case USER -> "user";
            case EMAIL_REGISTRATION -> "email-registration";
        };
        return prefix + ":" + identifier;
    }

    public CredentialManager.ResourceType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
}
