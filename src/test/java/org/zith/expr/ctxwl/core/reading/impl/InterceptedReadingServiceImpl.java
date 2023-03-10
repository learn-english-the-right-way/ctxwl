package org.zith.expr.ctxwl.core.reading.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.zith.expr.ctxwl.common.close.CombinedAutoCloseable;
import org.zith.expr.ctxwl.common.mongodb.MongoDbConfiguration;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.common.wordnet.WordNet;
import org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist.ReadingInducedWordlistRepository;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionFactory;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InterceptedReadingServiceImpl extends ReadingServiceImpl {

    private final ReadingSessionFactory readingSessionFactory;
    private final ConcurrentLinkedQueue<Interceptor> interceptors;

    protected InterceptedReadingServiceImpl(
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
        super(
                componentFactory,
                closeable,
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
        this.readingSessionFactory = readingSessionFactory;
        interceptors = new ConcurrentLinkedQueue<>();
    }

    public static InterceptedReadingServiceImpl create(
            ComponentFactory componentFactory,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MongoDbConfiguration mongoConfiguration,
            Clock clock
    ) {
        return create(
                InterceptedReadingServiceImpl::new,
                componentFactory,
                true,
                postgreSqlConfiguration,
                mongoConfiguration,
                clock
        );
    }

    Optional<Interceptor.Cancellation> inject(Interceptor interceptor) {
        interceptors.add(interceptor);
        interceptor.configure(readingSessionFactory);
        return Optional.of(new SimpleCancellation(interceptor));
    }

    private class SimpleCancellation implements Interceptor.Cancellation {
        private final Interceptor interceptor;

        public SimpleCancellation(Interceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void cancel() {
            interceptors.remove(interceptor);
        }
    }

    interface Interceptor {
        void configure(ReadingSessionFactory readingSessionFactory);

        interface Cancellation {
            void cancel();
        }
    }
}
