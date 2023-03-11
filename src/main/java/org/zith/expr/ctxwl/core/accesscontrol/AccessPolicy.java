package org.zith.expr.ctxwl.core.accesscontrol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface AccessPolicy {
    boolean isPrincipalInRole(Principal principal, Role role);

    boolean isPrincipalInRole(Principal principal, String role);

    Optional<String> encodeRole(Role role);

    Optional<Role> decodeRole(String role);

    static AccessPolicy of(List<RoleAssigner> roleAssigners, RoleCodec roleCodec) {
        return new AccessPolicyImpl(List.copyOf(roleAssigners), Objects.requireNonNull(roleCodec));
    }
}
