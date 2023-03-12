package org.zith.expr.ctxwl.webapi.authentication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;

import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;

public class CtxwlPrincipal implements Principal {
    private final org.zith.expr.ctxwl.core.accesscontrol.Principal defaultPrincipal;
    private final List<org.zith.expr.ctxwl.core.accesscontrol.Principal> auxiliaryPrincipals;

    private CtxwlPrincipal(
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

    public List<org.zith.expr.ctxwl.core.accesscontrol.Principal> getCompositingPrincipals() {
        return Stream.concat(Stream.of(defaultPrincipal), auxiliaryPrincipals.stream()).toList();
    }

    public Optional<org.zith.expr.ctxwl.core.accesscontrol.Principal>
    getCompositingPrincipal(ControlledResourceType type) {
        return Stream.concat(Stream.of(defaultPrincipal), auxiliaryPrincipals.stream())
                .filter(p -> p.resourceIdentifier().type() == type)
                .findFirst();
    }

    @NotNull
    static CtxwlPrincipal create(
            org.zith.expr.ctxwl.core.accesscontrol.Principal defaultPrincipal,
            List<org.zith.expr.ctxwl.core.accesscontrol.Principal> auxiliaryPrincipals) {
        return new CtxwlPrincipal(
                Objects.requireNonNull(defaultPrincipal),
                Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(auxiliaryPrincipals)))
        );
    }

    public static List<org.zith.expr.ctxwl.core.accesscontrol.Principal> decompose(@Nullable Principal principal) {
        return Optional.ofNullable(principal)
                .filter(CtxwlPrincipal.class::isInstance)
                .map(CtxwlPrincipal.class::cast).stream()
                .map(CtxwlPrincipal::getCompositingPrincipals)
                .flatMap(Collection::stream)
                .toList();
    }
}
