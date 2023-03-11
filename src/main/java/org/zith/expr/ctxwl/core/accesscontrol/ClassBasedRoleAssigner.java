package org.zith.expr.ctxwl.core.accesscontrol;

import java.util.function.BiFunction;

class ClassBasedRoleAssigner<R extends Role> implements RoleAssigner {
    private final Class<R> clazz;
    private final BiFunction<Principal, R, Boolean> canAssume;

    ClassBasedRoleAssigner(Class<R> clazz, BiFunction<Principal, R, Boolean> canAssume) {
        this.clazz = clazz;
        this.canAssume = canAssume;
    }

    @Override
    public boolean canAssume(Principal principal, Role role) {
        if (clazz.isInstance(role)) {
            return canAssume.apply(principal, clazz.cast(role));
        } else {
            return false;
        }
    }
}
