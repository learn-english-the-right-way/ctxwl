package org.zith.expr.ctxwl.core.reading.impl;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.*;

import java.time.Clock;

public class InterceptedComponentFactory implements ComponentFactory {
    @Override
    public @NotNull InterceptedReadingServiceImpl createReadingServiceImpl(
            boolean reinitializeData, PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            @NotNull Clock clock
    ) {
        return InterceptedReadingServiceImpl.create(this, postgreSqlConfiguration, mongoConfiguration, clock);
    }

    @Override
    public @NotNull InterceptedReadingSessionFactoryImpl createReadingSessionFactoryImpl(
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            Clock clock
    ) {
        return InterceptedReadingSessionFactoryImpl.create(this, sessionFactory, readingHistoryEntryRepository, clock);
    }

    @Override
    public @NotNull ReadingSessionImpl createReadingSessionImpl(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity readingSessionEntity) {
        return InterceptedReadingSessionImpl.create(readingSessionFactory, readingSessionEntity);
    }
}
