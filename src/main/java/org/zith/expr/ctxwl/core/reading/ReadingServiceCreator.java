package org.zith.expr.ctxwl.core.reading;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.DefaultComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.ReadingServiceImpl;

public final class ReadingServiceCreator {
    public static ReadingService create(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration
    ) {
        return create(new DefaultComponentFactory(), postgreSqlConfiguration, mongoConfiguration);
    }

    @NotNull
    static ReadingServiceImpl create(
            ComponentFactory componentFactory,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration
    ) {
        Preconditions.checkNotNull(componentFactory);
        Preconditions.checkNotNull(postgreSqlConfiguration);
        Preconditions.checkNotNull(mongoConfiguration);
        return componentFactory.createReadingServiceImpl(
                postgreSqlConfiguration,
                mongoConfiguration,
                componentFactory.createClock()
        );
    }
}
