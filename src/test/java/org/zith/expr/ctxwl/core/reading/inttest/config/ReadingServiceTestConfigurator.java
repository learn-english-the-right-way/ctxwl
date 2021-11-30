package org.zith.expr.ctxwl.core.reading.inttest.config;

import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.configuration.AbstractConfigurator;

public class ReadingServiceTestConfigurator extends AbstractConfigurator<ReadingServiceTestConfiguration> {
    @Override
    protected @NotNull ReadingServiceTestConfiguration emptyConfiguration() {
        return ReadingServiceTestConfiguration.empty();
    }

    @Override
    protected Class<ReadingServiceTestConfiguration> configurationClass() {
        return ReadingServiceTestConfiguration.class;
    }
}
