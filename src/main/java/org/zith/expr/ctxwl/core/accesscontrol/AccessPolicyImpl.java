package org.zith.expr.ctxwl.core.accesscontrol;

import java.util.List;
import java.util.Optional;

public class AccessPolicyImpl implements AccessPolicy {
    private final List<RoleAssigner> roleAssigners;
    private final RoleCodec roleCodec;

    AccessPolicyImpl(List<RoleAssigner> roleAssigners, RoleCodec roleCodec) {
        this.roleAssigners = roleAssigners;
        this.roleCodec = roleCodec;
    }

    @Override
    public boolean isPrincipalInRole(Principal principal, Role role) {
        return roleAssigners.stream().anyMatch(a -> a.canAssume(principal, role));
    }

    @Override
    public boolean isPrincipalInRole(Principal principal, String role) {
        return decodeRole(role).stream().anyMatch(r -> isPrincipalInRole(principal, r));
    }

    @Override
    public Optional<String> encodeRole(Role role) {
        return roleCodec.encode(role);
    }

    @Override
    public Optional<Role> decodeRole(String role) {
        return roleCodec.decode(role);
    }
}
