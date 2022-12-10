package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

abstract class AbstractApplicationKeyRole extends AbstractRole implements ApplicationKeyRole {
    private final Supplier<String> nameSupplier =
            Suppliers.memoize(() -> ApplicationKeyRole.name(applicationKey()));

    @Override
    public @NotNull String name() {
        return nameSupplier.get();
    }
}
