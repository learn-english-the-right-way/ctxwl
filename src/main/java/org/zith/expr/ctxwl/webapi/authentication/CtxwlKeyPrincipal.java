package org.zith.expr.ctxwl.webapi.authentication;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;

import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;

public class CtxwlKeyPrincipal implements Principal {
    private final org.zith.expr.ctxwl.core.accesscontrol.Principal defaultPrincipal;
    private final List<org.zith.expr.ctxwl.core.accesscontrol.Principal> auxiliaryPrincipals;

    private CtxwlKeyPrincipal(
            org.zith.expr.ctxwl.core.accesscontrol.Principal defaultPrincipal,
            List<org.zith.expr.ctxwl.core.accesscontrol.Principal> auxiliaryPrincipals
    ) {
        this.defaultPrincipal = defaultPrincipal;
        this.auxiliaryPrincipals = auxiliaryPrincipals;
    }

    @Override
    public String getName() {
        return defaultPrincipal.name();
    }

    @NotNull
    public org.zith.expr.ctxwl.core.accesscontrol.Principal getCompositingDefaultPrincipal() {
        return defaultPrincipal;
    }

    public List<org.zith.expr.ctxwl.core.accesscontrol.Principal> getCompositingAuxiliaryPrincipals() {
        return auxiliaryPrincipals;
    }

    public static Optional<org.zith.expr.ctxwl.core.accesscontrol.Principal> resolveDelegate(Principal principal) {
        if (principal instanceof CtxwlKeyPrincipal ctxwlKeyPrincipal) {
            return Optional.of(ctxwlKeyPrincipal.getCompositingDefaultPrincipal());
        } else {
            return Optional.empty();
        }
    }

    public Optional<org.zith.expr.ctxwl.core.accesscontrol.Principal>
    getCompositingPrincipal(ControlledResourceType type) {
        return Stream.concat(Stream.of(defaultPrincipal), auxiliaryPrincipals.stream())
                .filter(p -> p.resourceIdentifier().type() == type)
                .findFirst();
    }

    @NotNull
    static CtxwlKeyPrincipal create(
            org.zith.expr.ctxwl.core.accesscontrol.Principal defaultPrincipal,
            List<org.zith.expr.ctxwl.core.accesscontrol.Principal> auxiliaryPrincipals) {
        return new CtxwlKeyPrincipal(
                Objects.requireNonNull(defaultPrincipal),
                Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(auxiliaryPrincipals)))
        );
    }
}
