package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.ws.rs.core.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;

import java.util.Objects;

public class CtxwlKeySecurityContext implements SecurityContext {
    public static final String AUTHENTICATION_SCHEMA_CTXWL_DEFAULT = "ctxwl-default";

    private final SecurityContext base;
    private final CtxwlKeyPrincipal principal;

    private CtxwlKeySecurityContext(SecurityContext base, CtxwlKeyPrincipal principal) {
        this.base = Objects.requireNonNull(base);
        this.principal = Objects.requireNonNull(principal);
    }

    @Override
    public CtxwlKeyPrincipal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.getDelegate().roles().stream().anyMatch(r -> r.name().equals(role));
    }

    @Override
    public boolean isSecure() {
        return base.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return AUTHENTICATION_SCHEMA_CTXWL_DEFAULT;
    }

    @NotNull
    static CtxwlKeySecurityContext create(SecurityContext base, Principal principal) {
        return new CtxwlKeySecurityContext(base, CtxwlKeyPrincipal.create(principal));
    }

}
