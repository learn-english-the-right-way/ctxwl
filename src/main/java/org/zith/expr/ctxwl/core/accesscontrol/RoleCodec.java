package org.zith.expr.ctxwl.core.accesscontrol;

import java.util.Optional;

public interface RoleCodec {
    Optional<String> encode(Role role);

    Optional<Role> decode(String role);

}
