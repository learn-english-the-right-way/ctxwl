package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.common.async.UnbufferedProcessor;
import org.zith.expr.ctxwl.common.hibernate.DataAccessor;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.ReadingEvents;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.BoundReadingInspiredLookup;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.function.Function;

public class ReadingSessionFactoryImpl implements ReadingSessionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ReadingSessionFactoryImpl.class);

    private final ComponentFactory componentFactory;
    private final SessionFactory sessionFactory;
    private final ReadingHistoryEntryRepository readingHistoryEntryRepository;
    private final ReadingInspiredLookupRepository readingInspiredLookupRepository;
    private final Clock clock;
    private final DataAccessor.Factory dataAccessorFactory;

    public ReadingSessionFactoryImpl(
            ComponentFactory componentFactory,
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            ReadingInspiredLookupRepository readingInspiredLookupRepository,
            Clock clock
    ) {
        this.componentFactory = componentFactory;
        this.sessionFactory = sessionFactory;
        this.readingHistoryEntryRepository = readingHistoryEntryRepository;
        this.readingInspiredLookupRepository = readingInspiredLookupRepository;
        this.clock = clock;
        dataAccessorFactory = DataAccessor.Factory.of(
                e -> e instanceof OptimisticLockException || e instanceof SqlTransactionSerializationException,
                5
        );
    }

    final <T> T withTransaction(Function<Session, T> operation) {
        return dataAccessorFactory.create(operation).execute(sessionFactory);
    }

    @Override
    public @NotNull ReadingSessionImpl makeSession(String group, String wordlist) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        var readingSessionEntity = withTransaction(session -> {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(ReadingSessionEntity.class);
            var r = q.from(ReadingSessionEntity.class);
            q.where(cb.and(
                    cb.equal(r.get(ReadingSessionEntity_.group), group),
                    cb.equal(r.get(ReadingSessionEntity_.status), ReadingSessionImpl.Status.FRESH.getCode())));
            var optionalEntity = session.createQuery(q).uniqueResultOptional();
            if (optionalEntity.isEmpty()) {
                var entity = new ReadingSessionEntity();
                entity.setGroup(group);
                entity.setSerial(0L);
                entity.setStatus(ReadingSessionImpl.Status.FRESH.getCode());
                entity.setWordlist(wordlist);
                interceptInsertion(entity);
                try {
                    session.persist(entity);
                } catch (PersistenceException e) {
                    if (e.getCause() instanceof ConstraintViolationException ex &&
                            Objects.equals(ex.getConstraintName(), ReadingSessionEntity.INDEX_GROUP_SERIAL)) {
                        throw new SqlTransactionSerializationException(e);
                    }
                }
                optionalEntity = Optional.of(entity);
            }

            var entity = optionalEntity.get();
            entity.setStatus(ReadingSessionImpl.Status.ACTIVE.getCode());
            entity.setCreationTime(clock.instant().truncatedTo(ChronoUnit.MICROS));
            session.persist(entity);

            var placeholderEntity = new ReadingSessionEntity();
            placeholderEntity.setGroup(group);
            placeholderEntity.setSerial(entity.getSerial() + 1);
            placeholderEntity.setStatus(ReadingSessionImpl.Status.FRESH.getCode());
            interceptRotation(entity, placeholderEntity);
            session.persist(placeholderEntity);

            return optionalEntity.get();
        });

        return createReadingSession(readingSessionEntity);
    }

    @Override
    public Optional<ReadingSession> loadSession(String group, long serial) {
        // TODO add a cache to reduce PostgreSQL workload, make the cache aware of session status changes.
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        var optionalReadingSessionEntity = withTransaction(session -> {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(ReadingSessionEntity.class);
            var r = q.from(ReadingSessionEntity.class);
            q.where(cb.and(
                    cb.equal(r.get(ReadingSessionEntity_.group), group),
                    cb.equal(r.get(ReadingSessionEntity_.serial), serial),
                    cb.notEqual(r.get(ReadingSessionEntity_.status), ReadingSessionImpl.Status.FRESH.getCode())));
            return session.createQuery(q).uniqueResultOptional();
        });

        return optionalReadingSessionEntity.map(this::createReadingSession);
    }

    @Override
    public Flow.Publisher<Tracked<ReadingEvent>> collect(Executor executor) {
        var upstream = readingInspiredLookupRepository.collect(
                executor,
                (group, serial) -> loadSession(group, serial).orElseThrow(() -> new IllegalStateException(
                        "Dangling session reference %s %d".formatted(group, serial))));
        var processor =
                new UnbufferedProcessor<Tracked<BoundReadingInspiredLookup<ReadingSession>>, Tracked<ReadingEvent>>(
                        executor, item -> item.map(ReadingEvents.AddingReadingInspiredLookup::new));
        upstream.subscribe(processor);
        return processor;
    }

    @NotNull
    private ReadingSessionImpl createReadingSession(
            ReadingSessionEntity readingSessionEntity
    ) {
        var session = componentFactory.createReadingSessionImpl(
                this,
                readingSessionEntity
        );
        interceptCreatingSession(session);
        return session;
    }

    protected void interceptInsertion(ReadingSessionEntity entity) {
    }

    protected void interceptRotation(ReadingSessionEntity entity, ReadingSessionEntity placeholderEntity) {
    }

    protected void interceptCreatingSession(ReadingSessionImpl readingSession) {
    }

    public static ReadingSessionFactoryImpl create(
            ComponentFactory componentFactory,
            SessionFactory sessionFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            ReadingInspiredLookupRepository readingInspiredLookupRepository,
            Clock clock
    ) {
        return new ReadingSessionFactoryImpl(
                componentFactory,
                sessionFactory,
                readingHistoryEntryRepository,
                readingInspiredLookupRepository,
                clock
        );
    }

    ReadingHistoryEntryRepository getReadingHistoryEntryRepository() {
        return readingHistoryEntryRepository;
    }

    ReadingInspiredLookupRepository getReadingInspiredLookupRepository() {
        return readingInspiredLookupRepository;
    }

    Clock getClock() {
        return clock;
    }

    Duration getTimeDifferenceTolerance() {
        return Duration.ofMinutes(2); // TODO use a dynamic value
    }
}
