package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.accesscontrol.AccessPolicy;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;
import org.zith.expr.ctxwl.core.accesscontrol.Role;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CtxwlKeySecurityContext implements SecurityContext {
    public static final String AUTHENTICATION_SCHEMA_CTXWL_DEFAULT = "ctxwl-default";

    private final SecurityContext base;
    private final AccessPolicy policy;
    private final CtxwlKeyPrincipal principal;

    private CtxwlKeySecurityContext(SecurityContext base, AccessPolicy policy, CtxwlKeyPrincipal principal) {
        this.base = Objects.requireNonNull(base);
        this.policy = policy;
        this.principal = Objects.requireNonNull(principal);
    }

    @Override
    public CtxwlKeyPrincipal getUserPrincipal() {
        return principal;
    }

    @NotNull
    private Stream<Principal> principalStream() {
        return Stream.concat(
                Stream.of(principal.getCompositingDefaultPrincipal()),
                principal.getCompositingAuxiliaryPrincipals().stream());
    }

    @Override
    public boolean isUserInRole(String role) {
        return principalStream().anyMatch(principal -> policy.isPrincipalInRole(principal, role));
    }

    public boolean isUserInRole(Role role) {
        return principalStream().anyMatch(principal -> policy.isPrincipalInRole(principal, role));
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
    static CtxwlKeySecurityContext create(SecurityContext base, AccessPolicy policy, List<Principal> principals) {
        Objects.requireNonNull(policy);
        Preconditions.checkArgument(!principals.isEmpty());
        return new CtxwlKeySecurityContext(
                base, policy, CtxwlKeyPrincipal.create(principals.get(0), principals.subList(1, principals.size())));
    }
}
