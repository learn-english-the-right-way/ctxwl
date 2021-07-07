package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class Authentication {
    public static final String AUTHENTICATION_SCHEMA_CTXWL_DEFAULT = "ctxwl-default";

    private Authentication() {
    }

    public static List<Principal> principals(SecurityContext securityContext) {
        if (Objects.equals(securityContext.getAuthenticationScheme(), AUTHENTICATION_SCHEMA_CTXWL_DEFAULT)) {
            var results = new HashSet<Principal>();
            var unexpanded = new HashSet<Principal>();
            unexpanded.add(securityContext.getUserPrincipal());
            while (!unexpanded.isEmpty()) {
                var principal = unexpanded.iterator().next();
                unexpanded.remove(principal);
                results.add(principal);
                if (principal instanceof MixedPrincipal mixedPrincipal) {
                    mixedPrincipal.getPrincipals().stream()
                            .filter(p -> !results.contains(p))
                            .forEach(unexpanded::add);
                }
            }
            return results.stream().toList();
        } else {
            return List.of();
        }
    }
}
