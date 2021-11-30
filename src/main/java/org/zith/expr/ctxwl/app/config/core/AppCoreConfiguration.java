package org.zith.expr.ctxwl.app.config.core;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.app.config.core.identity.AppCoreIdentityConfiguration;
import org.zith.expr.ctxwl.app.config.core.reading.AppCoreReadingServiceConfiguration;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppCoreConfiguration(AppCoreIdentityConfiguration identity, AppCoreReadingServiceConfiguration reading)
        implements Configuration<AppCoreConfiguration> {
    @Override
    public AppCoreConfiguration merge(AppCoreConfiguration overriding) {
        return new AppCoreConfiguration(
                Configurations.merge(identity(), overriding.identity()),
                Configurations.merge(reading(), overriding.reading()));
    }

    public static AppCoreConfiguration empty() {
        return new AppCoreConfiguration(
                AppCoreIdentityConfiguration.empty(),
                AppCoreReadingServiceConfiguration.empty()
        );
    }
}
