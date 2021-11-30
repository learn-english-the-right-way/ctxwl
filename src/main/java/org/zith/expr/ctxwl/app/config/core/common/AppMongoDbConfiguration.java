package org.zith.expr.ctxwl.app.config.core.common;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;

public record AppMongoDbConfiguration(
        String uri
) implements Configuration<AppMongoDbConfiguration> {

    @Override
    public AppMongoDbConfiguration merge(AppMongoDbConfiguration overriding) {
        return new AppMongoDbConfiguration(
                Configurations.overlay(uri(), overriding.uri())
        );
    }

    public static AppMongoDbConfiguration empty() {
        return new AppMongoDbConfiguration(null);
    }

    public MongoDbConfiguration effectiveConfiguration() {
        return new MongoDbConfiguration(uri);
    }
}
