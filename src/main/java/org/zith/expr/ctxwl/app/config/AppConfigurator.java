package org.zith.expr.ctxwl.app.config;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.configuration.AbstractConfigurator;

public final class AppConfigurator extends AbstractConfigurator<AppConfiguration> {

    @NotNull
    @Override
    protected AppConfiguration emptyConfiguration() {
        return AppConfiguration.empty();
    }

    @Override
    protected Class<AppConfiguration> configurationClass() {
        return AppConfiguration.class;
    }
}
