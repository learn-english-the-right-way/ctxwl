package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

public class DefaultSecurityContext implements SecurityContext {
    private final SecurityContext base;
    private final List<SimplePrincipal> userPrincipals;

    private DefaultSecurityContext(SecurityContext base, List<SimplePrincipal> userPrincipals) {
        Preconditions.checkNotNull(base);
        Preconditions.checkArgument(!userPrincipals.isEmpty());

        this.base = base;
        this.userPrincipals = userPrincipals.stream().sorted().toList();
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipals.get(0);
    }

    @Override
    public boolean isUserInRole(String role) {
        if (role.startsWith("principal:")) {
            return userPrincipals.stream().anyMatch(p -> Objects.equals(role, "principal:" + p.getName()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isSecure() {
        return base.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return Authentication.AUTHENTICATION_SCHEMA_CTXWL_DEFAULT;
    }

    static DefaultSecurityContext create(SecurityContext base, CredentialManager.Domain domain, List<ControlledResource> principals) {
        var sortedPrincipals =
                principals.stream()
                        .sorted((a, b) -> {
                            var pa = principalTypePrecedence(domain, a);
                            var pb = principalTypePrecedence(domain, b);
                            if (pa < pb) return -1;
                            else if (pa > pb) return 1;
                            else return a.getIdentifier().compareTo(b.getIdentifier());
                        })
                        .map(v -> makePrincipal(v.getType(), v.getIdentifier()))
                        .toList();

        return new DefaultSecurityContext(base, sortedPrincipals);
    }

    private static SimplePrincipal makePrincipal(CredentialManager.ResourceType type, String identifier) {
        return new SimplePrincipal(type, identifier);
    }

    private static int principalTypePrecedence(CredentialManager.Domain domain, ControlledResource resource) {
        var principalTypes = domain.getPrincipalTypes();
        for (int i = 0; i < principalTypes.size(); i++) {
            if (principalTypes.get(i).reflectiveType() == resource.getType()) {
                return i;
            }
        }
        return -1;
    }
}
