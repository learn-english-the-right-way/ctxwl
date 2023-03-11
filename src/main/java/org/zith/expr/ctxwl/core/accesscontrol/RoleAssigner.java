package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public interface RoleAssigner {
    boolean canAssume(Principal principal, Role role);

    @NotNull
    static <R extends Role> RoleAssigner forClass(Class<R> clazz, BiFunction<Principal, R, Boolean> canAssume) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(canAssume);
        Preconditions.checkArgument(Role.class.isAssignableFrom(clazz));
        return new ClassBasedRoleAssigner<>(clazz, canAssume);
    }

}
