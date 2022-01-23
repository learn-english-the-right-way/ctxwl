package org.zith.expr.ctxwl.core.reading.impl;

import com.mongodb.client.MongoDatabase;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryDocument;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepositoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionEntity;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionImpl;

import java.time.Clock;

public interface ComponentFactory {
    @NotNull
    default ReadingSessionFactoryImpl createReadingSessionFactoryImpl(
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            Clock clock
    ) {
        return ReadingSessionFactoryImpl.create(this, sessionFactory, readingHistoryEntryRepository, clock);
    }

    @NotNull
    default ReadingSessionImpl createReadingSessionImpl(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity readingSessionEntity) {
        return ReadingSessionImpl.create(readingSessionFactory, readingSessionEntity);
    }

    @NotNull
    default ReadingServiceImpl createReadingServiceImpl(
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        return ReadingServiceImpl.create(this, postgreSqlConfiguration, mongoConfiguration, clock);
    }

    @NotNull
    default Clock createClock() {
        return Clock.systemDefaultZone();
    }

    default ReadingHistoryEntryRepositoryImpl createReadingHistoryEntryRepositoryImpl(MongoDatabase mongoDatabase) {
        return ReadingHistoryEntryRepositoryImpl.create(this, mongoDatabase);
    }

    @NotNull
    default <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> createReadingHistoryEntryImpl(
            ReadingHistoryEntryRepositoryImpl repository,
            Session session,
            long serial,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        return ReadingHistoryEntryImpl.create(repository, session, serial, document);
    }
}