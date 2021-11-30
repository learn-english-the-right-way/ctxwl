package org.zith.expr.ctxwl.webapi.authentication;

import org.jetbrains.annotations.NotNull;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

public class CtxwlKeyPrincipal implements Principal {
    private final org.zith.expr.ctxwl.webapi.access.Principal delegate;

    private CtxwlKeyPrincipal(org.zith.expr.ctxwl.webapi.access.Principal principal) {
        delegate = principal;
    }

    @Override
    public String getName() {
        return delegate.subject().name();
    }

    @NotNull
    public org.zith.expr.ctxwl.webapi.access.Principal getDelegate() {
        return delegate;
    }

    public static Optional<org.zith.expr.ctxwl.webapi.access.Principal> resolveDelegate(Principal principal) {
        if (principal instanceof CtxwlKeyPrincipal ctxwlKeyPrincipal) {
            return Optional.of(ctxwlKeyPrincipal.getDelegate());
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    static CtxwlKeyPrincipal create(org.zith.expr.ctxwl.webapi.access.Principal principal) {
        return new CtxwlKeyPrincipal(Objects.requireNonNull(principal));
    }
}
