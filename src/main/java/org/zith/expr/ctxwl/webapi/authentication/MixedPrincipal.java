package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Preconditions;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

public class MixedPrincipal implements Principal {
    private final List<Principal> principals;

    MixedPrincipal(List<Principal> principals) {
        Objects.requireNonNull(principals);
        Preconditions.checkArgument(!principals.isEmpty());
        this.principals = List.copyOf(principals);
    }

    @Override
    public String getName() {
        return principals.get(0).getName();
    }

    public List<Principal> getPrincipals() {
        return principals;
    }
}
