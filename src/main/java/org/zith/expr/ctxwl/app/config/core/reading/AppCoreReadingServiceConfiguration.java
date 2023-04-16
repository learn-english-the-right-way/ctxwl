package org.zith.expr.ctxwl.app.config.core.reading;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.app.config.core.common.AppMongoDbConfiguration;
import org.zith.expr.ctxwl.app.config.core.common.AppPostgreSqlConfiguration;
import org.zith.expr.ctxwl.app.config.core.paragraphgenerator.AppOpenAIConfiguration;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record AppCoreReadingServiceConfiguration(
        Boolean reinitializeData,
        AppPostgreSqlConfiguration postgreSql,
        AppMongoDbConfiguration mongoDb,
        AppOpenAIConfiguration openAI
) implements Configuration<AppCoreReadingServiceConfiguration> {
    @Override
    public AppCoreReadingServiceConfiguration merge(AppCoreReadingServiceConfiguration overriding) {
        return new AppCoreReadingServiceConfiguration(
                Configurations.overlay(reinitializeData, overriding.reinitializeData),
                Configurations.merge(postgreSql(), overriding.postgreSql()),
                Configurations.merge(mongoDb(), overriding.mongoDb()),
                Configurations.merge(openAI(), overriding.openAI())
        );
    }

    public static AppCoreReadingServiceConfiguration empty() {
        return new AppCoreReadingServiceConfiguration(
                null,
                AppPostgreSqlConfiguration.empty(),
                AppMongoDbConfiguration.empty(),
                AppOpenAIConfiguration.empty()
        );
    }
}
