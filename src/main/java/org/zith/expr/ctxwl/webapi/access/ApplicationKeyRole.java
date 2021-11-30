package org.zith.expr.ctxwl.webapi.access;

import org.jetbrains.annotations.NotNull;

public interface ApplicationKeyRole extends Role {

    @NotNull ActiveResourceRole activeResourceRole();

    @NotNull String applicationKey();

    @NotNull
    static String name(@NotNull String applicationKey) {
        return "application-key:" + AbstractRole.escape(applicationKey);
    }
}
