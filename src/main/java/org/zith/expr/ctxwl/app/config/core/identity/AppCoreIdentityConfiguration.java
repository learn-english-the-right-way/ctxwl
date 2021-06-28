package org.zith.expr.ctxwl.app.config.core.identity;

import org.zith.expr.ctxwl.app.config.Configuration;
import org.zith.expr.ctxwl.app.config.Configurations;

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
