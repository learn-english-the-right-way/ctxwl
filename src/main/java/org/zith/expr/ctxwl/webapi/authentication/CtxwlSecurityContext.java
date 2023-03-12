package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.accesscontrol.AccessPolicy;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;

import java.util.List;
import java.util.Objects;

public class CtxwlSecurityContext implements SecurityContext {
    public static final String AUTHENTICATION_SCHEMA_CTXWL_DEFAULT = "ctxwl-default";

    private final SecurityContext base;
    private final AccessPolicy policy;
    private final CtxwlPrincipal principal;

    private CtxwlSecurityContext(SecurityContext base, AccessPolicy policy, CtxwlPrincipal principal) {
        this.base = Objects.requireNonNull(base);
        this.policy = policy;
        this.principal = Objects.requireNonNull(principal);
    }

    @Override
    public CtxwlPrincipal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.getCompositingPrincipals().stream()
                .anyMatch(principal -> policy.isPrincipalInRole(principal, role));
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
    static CtxwlSecurityContext create(SecurityContext base, AccessPolicy policy, List<Principal> principals) {
        Objects.requireNonNull(policy);
        Preconditions.checkArgument(!principals.isEmpty());
        return new CtxwlSecurityContext(
                base, policy, CtxwlPrincipal.create(principals.get(0), principals.subList(1, principals.size())));
    }
}
