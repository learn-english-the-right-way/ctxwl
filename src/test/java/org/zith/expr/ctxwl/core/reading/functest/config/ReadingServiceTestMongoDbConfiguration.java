package org.zith.expr.ctxwl.core.reading.functest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;

public record ReadingServiceTestMongoDbConfiguration(
        String uri
) implements Configuration<ReadingServiceTestMongoDbConfiguration> {

    @Override
    public ReadingServiceTestMongoDbConfiguration merge(ReadingServiceTestMongoDbConfiguration overriding) {
        return new ReadingServiceTestMongoDbConfiguration(
                Configurations.overlay(uri(), overriding.uri())
        );
    }

    public static ReadingServiceTestMongoDbConfiguration empty() {
        return new ReadingServiceTestMongoDbConfiguration(null);
    }

    public MongoDbConfiguration effectiveConfiguration() {
        return new MongoDbConfiguration(uri);
    }
}
