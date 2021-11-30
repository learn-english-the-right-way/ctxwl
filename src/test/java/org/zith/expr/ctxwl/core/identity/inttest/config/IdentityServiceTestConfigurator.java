package org.zith.expr.ctxwl.core.identity.inttest.config;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.configuration.AbstractConfigurator;

public class IdentityServiceTestConfigurator extends AbstractConfigurator<IdentityServiceTestConfiguration> {
    @Override
    protected @NotNull IdentityServiceTestConfiguration emptyConfiguration() {
        return IdentityServiceTestConfiguration.empty();
    }

    @Override
    protected Class<IdentityServiceTestConfiguration> configurationClass() {
        return IdentityServiceTestConfiguration.class;
    }
}
