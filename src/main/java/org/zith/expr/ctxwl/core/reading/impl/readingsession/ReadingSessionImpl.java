package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import com.google.common.base.Preconditions;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class ReadingSessionImpl implements ReadingSession {

    private final ReadingSessionFactoryImpl factory;
    private final ReadingSessionEntity entity;

    public ReadingSessionImpl(
            ReadingSessionFactoryImpl factory,
            ReadingSessionEntity entity
    ) {
        this.factory = factory;
        this.entity = entity;
    }

    public static ReadingSessionImpl create(
            ReadingSessionFactoryImpl readingSessionFactory,
            ReadingSessionEntity entity
    ) {
        return new ReadingSessionImpl(readingSessionFactory, entity);
    }

    @Override
    public String getGroup() {
        return entity.getGroup();
    }

    @Override
    public long getSerial() {
        return entity.getSerial();
    }

    @Override
    public void complete(Instant timestamp) {
        Preconditions.checkNotNull(timestamp);
        Preconditions.checkArgument(timestamp.equals(timestamp.truncatedTo(ChronoUnit.MICROS)));

        factory.withTransaction(session -> {
            session.refresh(entity);
            switch (getEntityStatus()) {
                case ACTIVE -> {
                    if (Duration.between(timestamp, factory.getClock().instant()).abs()
                            .compareTo(factory.getTimeDifferenceTolerance()) > 0) {
                        throw new IllegalArgumentException(
                                "The offered time of completion differs too much from the server time");
                    }
                    entity.setStatus(Status.TERMINATING.getCode());
                    entity.setCompletionTime(timestamp);
                    session.persist(entity);
                }
                case TERMINATING, TERMINATED -> {
                    if (!Objects.equals(entity.getCompletionTime(), timestamp)) {
                        throw new IllegalStateException("The session has already been completed");
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + getEntityStatus());
            }
            return null;
        });
    }

    @Override
    public ReadingHistoryEntry create(long serial, ReadingHistoryEntryValue value) {
        return factory.getReadingHistoryEntryRepository().create(this, serial, value);
    }

    @Override
    public Optional<Instant> getUpdateTime() {
        return Optional.ofNullable(entity.getUpdateTime());
    }

    @Override
    public Optional<Instant> getCompletionTime() {
        return Optional.ofNullable(entity.getCompletionTime());
    }

    @Override
    public Optional<Instant> getTerminationTime() {
        return Optional.ofNullable(entity.getTerminationTime());
    }

    private Status getEntityStatus() {
        return Arrays.stream(Status.values())
                .filter(s -> s != Status._UNKNOWN)
                .filter(s -> Objects.equals(s.getCode(), entity.getStatus()))
                .findAny()
                .orElse(Status._UNKNOWN);
    }

    enum Status {
        FRESH("fresh"),
        ACTIVE("active"),
        TERMINATING("terminating"),
        TERMINATED("terminated"),
        _UNKNOWN(null);

        private final String code;

        Status(String code) {
            this.code = code;
        }

        public String getCode() {
            Preconditions.checkArgument(code != null);
            return code;
        }
    }
}
