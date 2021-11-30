package org.zith.expr.ctxwl.app.config.core.identity;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.app.config.core.common.AppMailConfiguration;
import org.zith.expr.ctxwl.app.config.core.common.AppPostgreSqlConfiguration;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppCoreIdentityConfiguration(AppPostgreSqlConfiguration postgreSql, AppMailConfiguration mail)
        implements Configuration<AppCoreIdentityConfiguration> {
    @Override
    public AppCoreIdentityConfiguration merge(AppCoreIdentityConfiguration overriding) {
        return new AppCoreIdentityConfiguration(
                Configurations.merge(postgreSql(), overriding.postgreSql()),
                Configurations.merge(mail(), overriding.mail())
        );
    }

    public static AppCoreIdentityConfiguration empty() {
        return new AppCoreIdentityConfiguration(
                AppPostgreSqlConfiguration.empty(),
                AppMailConfiguration.empty()
        );
    }
}
