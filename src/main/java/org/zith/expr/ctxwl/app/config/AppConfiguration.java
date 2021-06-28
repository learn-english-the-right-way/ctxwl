package org.zith.expr.ctxwl.app.config;

import org.zith.expr.ctxwl.app.config.core.AppCoreConfiguration;
import org.zith.expr.ctxwl.app.config.webapi.AppWebApiConfiguration;

public record AppConfiguration(
        AppCoreConfiguration core,
        AppWebApiConfiguration webApi
) implements Configuration<AppConfiguration> {

    @Override
    public AppConfiguration merge(AppConfiguration overriding) {
        return new AppConfiguration(
                Configurations.merge(core(), overriding.core()),
                Configurations.merge(webApi(), overriding.webApi())
        );
    }

    public static AppConfiguration empty() {
        return new AppConfiguration(
                AppCoreConfiguration.empty(),
                AppWebApiConfiguration.empty()
        );
    }
}
