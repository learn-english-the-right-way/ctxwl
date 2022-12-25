package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;
import org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup.ReadingInspiredLookupRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ReadingSessionFactoryImpl implements ReadingSessionFactory {

    private final ComponentFactory componentFactory;
    private final SessionFactory sessionFactory;
    private final ReadingHistoryEntryRepository readingHistoryEntryRepository;
    private final ReadingInspiredLookupRepository readingInspiredLookupRepository;
    private final Clock clock;

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
    }

    final <T> T withTransaction(Function<Session, T> operation) {
        for (int attempt = 0; ; ++attempt) {
            try {
                var result = Optional.<Optional<T>>empty();
                var session = sessionFactory.openSession();
                try {
                    var transaction = session.beginTransaction();
                    try {
                        var transientResult = operation.apply(session);
                        if (transaction.getRollbackOnly()) {
                            throw new IllegalStateException();
                        }
                        transaction.commit();
                        result = Optional.of(Optional.ofNullable(transientResult));
                    } catch (Exception e) {
                        try {
                            if (transaction.isActive()) {
                                transaction.rollback();
                            }
                        } catch (Exception ex) {
                            e.addSuppressed(ex);
                        }
                        throw e;
                    }
                } catch (Exception e) {
                    try {
                        if (session.isOpen()) {
                            session.close();
                        }
                    } catch (Exception ex) {
                        e.addSuppressed(ex);
                    }
                    throw e;
                }
                if (session.isOpen()) {
                    session.close();
                }
                return result.get().orElse(null);
            } catch (SqlTransactionSerializationException e) {
                if (attempt >= 5) {
                    throw e;
                }
            }
        }
    }

    @Override
    public @NotNull ReadingSessionImpl makeSession(String group) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        var readingSessionEntity = withTransaction(session -> {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(ReadingSessionEntity.class);
            var r = q.from(ReadingSessionEntity.class);
            q.where(cb.and(
                    cb.equal(r.get(ReadingSessionEntity_.group), group),
                    cb.equal(r.get(ReadingSessionEntity_.status), ReadingSessionImpl.Status.FRESH.getCode())));
            var optionalEntity =
                    session.createQuery(q).setLockMode(LockModeType.PESSIMISTIC_WRITE).uniqueResultOptional();
            if (optionalEntity.isEmpty()) {
                var entity = new ReadingSessionEntity();
                entity.setGroup(group);
                entity.setSerial(0L);
                entity.setStatus(ReadingSessionImpl.Status.FRESH.getCode());
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
            session.persist(placeholderEntity);

            return optionalEntity.get();
        });

        return createReadingSession(readingSessionEntity);
    }

    @Override
    public Optional<ReadingSession> loadSession(String group, long serial) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group));
        var optionalReadingSessionEntity = withTransaction(session -> {
            var cb = session.getCriteriaBuilder();
            var q = cb.createQuery(ReadingSessionEntity.class);
            var r = q.from(ReadingSessionEntity.class);
            q.where(cb.and(
                    cb.equal(r.get(ReadingSessionEntity_.group), group),
                    cb.equal(r.get(ReadingSessionEntity_.serial), serial)));
            // TODO handle concurrency issues
            return session.createQuery(q).uniqueResultOptional();
        });

        return optionalReadingSessionEntity.map(this::createReadingSession);
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
