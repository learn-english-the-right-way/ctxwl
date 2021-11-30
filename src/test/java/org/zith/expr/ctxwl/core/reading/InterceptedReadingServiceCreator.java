package org.zith.expr.ctxwl.core.reading;

import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.reading.impl.InterceptedComponentFactory;

public final class InterceptedReadingServiceCreator {
    public static ReadingService create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration
    ) {
        return ReadingServiceCreator.create(
                new InterceptedComponentFactory(),
                postgreSqlConfiguration,
                mongoConfiguration
        );
    }
}
