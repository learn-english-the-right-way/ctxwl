package org.zith.expr.ctxwl.core.reading.impl;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.Action;
import org.zith.expr.ctxwl.common.hibernate.LowerUnderscorePhysicalNamingStrategy;
import org.zith.expr.ctxwl.common.hibernate.SuffixStripingImplicitNamingStrategy;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepositoryImpl;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionEntity;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactory;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

public class ReadingServiceImpl implements ReadingService {

    private final ComponentFactory componentFactory;
    private final DataSource dataSource;
    private final StandardServiceRegistry serviceRegistry;
    private final Metadata metadata;
    private final SessionFactory sessionFactory;
    private final ReadingSessionFactory readingSessionFactory;
    private final MongoDatabase mongoDatabase;

    protected ReadingServiceImpl(
            ComponentFactory componentFactory,
            DataSource dataSource,
            StandardServiceRegistry serviceRegistry,
            Metadata metadata,
            SessionFactory sessionFactory,
            ReadingSessionFactory readingSessionFactory,
            MongoDatabase mongoDatabase
    ) {
        this.componentFactory = componentFactory;
        this.dataSource = dataSource;
        this.serviceRegistry = serviceRegistry;
        this.metadata = metadata;
        this.sessionFactory = sessionFactory;
        this.readingSessionFactory = readingSessionFactory;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public ReadingSession makeSession(String group) {
        return readingSessionFactory.makeSession(group);
    }

    @Override
    public Optional<ReadingSession> loadSession(String group, long serial) {
        return readingSessionFactory.loadSession(group, serial);
    }

    public static ReadingServiceImpl create(
            ComponentFactory componentFactory,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        return create(ReadingServiceImpl::new, componentFactory, postgreSqlConfiguration, mongoConfiguration, clock);
    }

    protected static <T extends ReadingServiceImpl> T create(
            ImplementationFactory<T> implementationFactory,
            ComponentFactory componentFactory,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        var dataSource =
                postgreSqlConfiguration.makeDataSource(
                        PostgreSqlConfiguration.TransactionIsolation.TRANSACTION_READ_COMMITTED
                );
        var serviceRegistry =
                new StandardServiceRegistryBuilder()
                        .applySetting(AvailableSettings.DATASOURCE, dataSource)
                        .applySetting(AvailableSettings.HBM2DDL_AUTO, Action.CREATE_DROP) // TODO
                        .applySetting(AvailableSettings.KEYWORD_AUTO_QUOTING_ENABLED, true)
                        .build();
        var metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(ReadingSessionEntity.class)
                .getMetadataBuilder()
                .applyImplicitNamingStrategy(SuffixStripingImplicitNamingStrategy.stripEntitySuffix())
                .applyPhysicalNamingStrategy(new LowerUnderscorePhysicalNamingStrategy())
                .build();
        var sessionFactory = metadata.getSessionFactoryBuilder().build();

        var mongoDatabase =
                MongoClients.create(mongoConfiguration.makeMongoClientSettings())
                        .getDatabase(Objects.requireNonNull(mongoConfiguration.connectionString().getDatabase()));

        var readingHistoryEntryRepository =
                componentFactory.createReadingHistoryEntryRepositoryImpl(mongoDatabase);

        var readingSessionFactory =
                componentFactory.createReadingSessionFactoryImpl(sessionFactory, readingHistoryEntryRepository, clock);

        return implementationFactory.create(
                componentFactory,
                dataSource,
                serviceRegistry,
                metadata,
                sessionFactory,
                readingSessionFactory,
                mongoDatabase
        );
    }

    @Override
    public void close() throws Exception {
        sessionFactory.close();
        serviceRegistry.close();
    }

    protected interface ImplementationFactory<T extends ReadingServiceImpl> {
        T create(
                ComponentFactory componentFactory,
                DataSource dataSource,
                StandardServiceRegistry serviceRegistry,
                Metadata metadata,
                SessionFactory sessionFactory,
                ReadingSessionFactory readingSessionFactory,
                MongoDatabase mongoDatabase
        );
    }
}
