package org.zith.expr.ctxwl.app.config.core.reading;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.app.config.core.common.AppMongoDbConfiguration;
import org.zith.expr.ctxwl.app.config.core.common.AppPostgreSqlConfiguration;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppCoreReadingServiceConfiguration(
        AppPostgreSqlConfiguration postgreSql,
        AppMongoDbConfiguration mongoDb
) implements Configuration<AppCoreReadingServiceConfiguration> {
    @Override
    public AppCoreReadingServiceConfiguration merge(AppCoreReadingServiceConfiguration overriding) {
        return new AppCoreReadingServiceConfiguration(
                Configurations.merge(postgreSql(), overriding.postgreSql()),
                Configurations.merge(mongoDb(), overriding.mongoDb())
        );
    }

    public static AppCoreReadingServiceConfiguration empty() {
        return new AppCoreReadingServiceConfiguration(
                AppPostgreSqlConfiguration.empty(),
                AppMongoDbConfiguration.empty()
        );
    }
}
