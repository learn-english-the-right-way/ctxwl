package org.zith.expr.ctxwl.core.reading.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.common.close.CombinedAutoCloseable;
import org.zith.expr.ctxwl.common.hibernate.LowerUnderscorePhysicalNamingStrategy;
import org.zith.expr.ctxwl.common.hibernate.SuffixStripingImplicitNamingStrategy;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.common.wordnet.WordNet;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingInducedWordlist;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist.ReadingInducedWordlistEntryEntity;
import org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist.ReadingInducedWordlistRepository;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionEntity;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactory;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

public class ReadingServiceImpl implements ReadingService {

    private final ComponentFactory componentFactory;
    private final CombinedAutoCloseable closeable;
    private final DataSource dataSource;
    private final StandardServiceRegistry serviceRegistry;
    private final Metadata metadata;
    private final SessionFactory sessionFactory;
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final WordNet wordNet;
    private final ReadingSessionFactory readingSessionFactory;
    private final ReadingInducedWordlistRepository readingInducedWordlistRepository;

    protected ReadingServiceImpl(
            ComponentFactory componentFactory,
            CombinedAutoCloseable closeable,
            DataSource dataSource,
            StandardServiceRegistry serviceRegistry,
            Metadata metadata,
            SessionFactory sessionFactory,
            MongoClient mongoClient,
            MongoDatabase mongoDatabase,
            WordNet wordNet,
            ReadingSessionFactory readingSessionFactory,
            ReadingInducedWordlistRepository readingInducedWordlistRepository
    ) {
        this.componentFactory = componentFactory;
        this.closeable = closeable;
        this.dataSource = dataSource;
        this.serviceRegistry = serviceRegistry;
        this.metadata = metadata;
        this.sessionFactory = sessionFactory;
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.wordNet = wordNet;
        this.readingSessionFactory = readingSessionFactory;
        this.readingInducedWordlistRepository = readingInducedWordlistRepository;
    }

    @Override
    public ReadingSession makeSession(String group, String wordlist) {
        return readingSessionFactory.makeSession(group, wordlist);
    }

    @Override
    public Optional<ReadingSession> loadSession(String group, long serial) {
        return readingSessionFactory.loadSession(group, serial);
    }

    public static ReadingServiceImpl create(
            ComponentFactory componentFactory,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        return create(
                ReadingServiceImpl::new,
                componentFactory,
                reinitializeData,
                postgreSqlConfiguration,
                mongoConfiguration,
                clock
        );
    }

    @Override
    public Flow.Publisher<Tracked<ReadingEvent>> collect(Executor executor) {
        return readingSessionFactory.collect(executor);
    }

    @Override
    public ReadingInducedWordlist getWordlist(String id) {
        return readingInducedWordlistRepository.getWordlist(id);
    }

    @Override
    public void extendWordlist(List<ReadingEvent> events) {
        readingInducedWordlistRepository.consume(events);
    }

    protected static <T extends ReadingServiceImpl> T create(
            ImplementationFactory<T> implementationFactory,
            ComponentFactory componentFactory,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        try (var closeable = CombinedAutoCloseable.create()) {
            var dataSource =
                    postgreSqlConfiguration
                            .makeDataSource(PostgreSqlConfiguration.TransactionIsolation.TRANSACTION_REPEATABLE_READ);
            var serviceRegistryBuilder = new StandardServiceRegistryBuilder()
                    .applySetting(AvailableSettings.DATASOURCE, dataSource)
                    .applySetting(AvailableSettings.KEYWORD_AUTO_QUOTING_ENABLED, true);
            if (reinitializeData) {
                serviceRegistryBuilder.applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop");
            }
            var serviceRegistry = closeable.register(serviceRegistryBuilder.build());

            var metadata = new MetadataSources(serviceRegistry)
                    .addAnnotatedClass(ReadingSessionEntity.class)
                    .addAnnotatedClass(ReadingInducedWordlistEntryEntity.class)
                    .getMetadataBuilder()
                    .applyImplicitNamingStrategy(SuffixStripingImplicitNamingStrategy.stripEntitySuffix())
                    .applyPhysicalNamingStrategy(new LowerUnderscorePhysicalNamingStrategy())
                    .build();
            var sessionFactory = closeable.register(metadata.getSessionFactoryBuilder().build());

            var mongoClient = closeable.register(MongoClients.create(mongoConfiguration.makeMongoClientSettings()));

            var mongoDatabase =
                    mongoClient.getDatabase(Objects.requireNonNull(mongoConfiguration.connectionString().getDatabase()));

            var wordNet = closeable.register(new WordNet());

            var readingHistoryEntryRepository =
                    componentFactory.createReadingHistoryEntryRepositoryImpl(mongoDatabase, reinitializeData);

            var readingInspiredLookupRepository =
                    componentFactory.createReadingInspiredLookupRepositoryImpl(
                            readingHistoryEntryRepository, mongoDatabase, reinitializeData);

            var readingSessionFactory =
                    componentFactory.createReadingSessionFactoryImpl(
                            sessionFactory, readingHistoryEntryRepository, readingInspiredLookupRepository, clock);

            var readingInducedWordlistRepository =
                    componentFactory.createReadingInducedWordlistRepositoryImpl(
                            sessionFactory, wordNet);

            return implementationFactory.create(
                    componentFactory,
                    closeable.transfer(),
                    dataSource,
                    serviceRegistry,
                    metadata,
                    sessionFactory,
                    mongoClient,
                    mongoDatabase,
                    wordNet,
                    readingSessionFactory,
                    readingInducedWordlistRepository
            );
        }
    }

    @Override
    public void close() {
        closeable.close();
    }

    protected interface ImplementationFactory<T extends ReadingServiceImpl> {
        T create(
                ComponentFactory componentFactory,
                CombinedAutoCloseable closeable,
                DataSource dataSource,
                StandardServiceRegistry serviceRegistry,
                Metadata metadata,
                SessionFactory sessionFactory,
                MongoClient mongoClient,
                MongoDatabase mongoDatabase,
                WordNet wordNet,
                ReadingSessionFactory readingSessionFactory,
                ReadingInducedWordlistRepository readingInducedWordlistRepository
        );
    }
}
