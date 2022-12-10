package org.zith.expr.ctxwl.core.reading.functest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;

public record ReadingServiceTestConfiguration(
        ReadingServiceTestPostgreSqlConfiguration postgreSql,
        ReadingServiceTestMongoDbConfiguration mongoDb
) implements Configuration<ReadingServiceTestConfiguration> {
    @Override
    public ReadingServiceTestConfiguration merge(ReadingServiceTestConfiguration overriding) {
        return new ReadingServiceTestConfiguration(
                Configurations.merge(postgreSql(), overriding.postgreSql()),
                Configurations.merge(mongoDb(), overriding.mongoDb())
        );
    }

    public static ReadingServiceTestConfiguration empty() {
        return new ReadingServiceTestConfiguration(
                ReadingServiceTestPostgreSqlConfiguration.empty(),
                ReadingServiceTestMongoDbConfiguration.empty()
        );
    }
}
