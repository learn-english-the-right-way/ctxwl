package org.zith.expr.ctxwl.core.reading.impl;

import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.common.wordnet.WordNet;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.*;
import org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist.ReadingInducedWordlistRepositoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupDocument;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupImpl;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupRepository;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupRepositoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionEntity;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionImpl;

import java.time.Clock;

public interface ComponentFactory {
    @NotNull
    default ReadingSessionFactoryImpl createReadingSessionFactoryImpl(
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            ReadingInspiredLookupRepository readingInspiredLookupRepository,
            Clock clock
    ) {
        return ReadingSessionFactoryImpl.create(
                this,
                sessionFactory,
                readingHistoryEntryRepository,
                readingInspiredLookupRepository,
                clock
        );
    }

    @NotNull
    default ReadingSessionImpl createReadingSessionImpl(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity readingSessionEntity) {
        return ReadingSessionImpl.create(readingSessionFactory, readingSessionEntity);
    }

    @NotNull
    default ReadingServiceImpl createReadingServiceImpl(
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        return ReadingServiceImpl.create(this, reinitializeData, postgreSqlConfiguration, mongoConfiguration, clock);
    }

    @NotNull
    default Clock createClock() {
        return Clock.systemDefaultZone();
    }

    default ReadingHistoryEntryRepositoryImpl createReadingHistoryEntryRepositoryImpl(
            MongoDatabase mongoDatabase,
            boolean reinitializeData
    ) {
        return ReadingHistoryEntryRepositoryImpl.create(this, mongoDatabase, reinitializeData);
    }

    default ReadingInspiredLookupRepositoryImpl createReadingInspiredLookupRepositoryImpl(
            ReadingHistoryEntryRepository createReadingHistoryEntryRepository,
            MongoDatabase mongoDatabase,
            boolean reinitializeData
    ) {
        return ReadingInspiredLookupRepositoryImpl.create(
                this,
                createReadingHistoryEntryRepository,
                mongoDatabase,
                reinitializeData
        );
    }

    @NotNull
    default <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> createReadingHistoryEntryImpl(
            ReadingHistoryEntryRepositoryImpl repository,
            Session session,
            long serial,
            @Nullable ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        return ReadingHistoryEntryImpl.create(repository, session, serial, reference, document);
    }

    @NotNull
    default <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> createReadingHistoryEntryImpl(
            ReadingHistoryEntryRepositoryImpl repository,
            SessionProvider<Session> sessionProvider,
            ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        return ReadingHistoryEntryImpl.create(repository, sessionProvider, reference, document);
    }

    default <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> createReadingInspiredLookupImpl(
            ReadingInspiredLookupRepositoryImpl repository,
            Session session,
            long historyEntrySerial,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        return ReadingInspiredLookupImpl.create(repository, session, historyEntrySerial, serial, document);
    }

    default <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> createReadingInspiredLookupImpl(
            ReadingInspiredLookupRepositoryImpl repository,
            BoundReadingHistoryEntry<Session> historyEntry,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        return ReadingInspiredLookupImpl.create(repository, historyEntry, serial, document);
    }

    default ReadingInducedWordlistRepositoryImpl createReadingInducedWordlistRepositoryImpl(
            SessionFactory sessionFactory,
            WordNet wordNet
    ) {
        return new ReadingInducedWordlistRepositoryImpl(sessionFactory, wordNet);
    }
}
