package org.zith.expr.ctxwl.app.config.core;

import org.zith.expr.ctxwl.app.config.Configuration;
import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.app.config.core.identity.AppCoreIdentityConfiguration;

public record AppCoreConfiguration(AppCoreIdentityConfiguration identity)
        implements Configuration<AppCoreConfiguration> {
    @Override
    public AppCoreConfiguration merge(AppCoreConfiguration overriding) {
        return new AppCoreConfiguration(Configurations.merge(identity(), overriding.identity()));
    }

    public static AppCoreConfiguration empty() {
        return new AppCoreConfiguration(
                AppCoreIdentityConfiguration.empty()
        );
    }
}
