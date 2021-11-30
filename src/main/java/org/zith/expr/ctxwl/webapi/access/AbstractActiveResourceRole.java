package org.zith.expr.ctxwl.webapi.access;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

abstract class AbstractActiveResourceRole extends AbstractRole implements ActiveResourceRole {
    private final Supplier<String> nameSupplier =
            Suppliers.memoize(() -> ActiveResourceRole.name(resourceType(), identifier()));

    @Override
    public @NotNull String name() {
        return nameSupplier.get();
    }

}
