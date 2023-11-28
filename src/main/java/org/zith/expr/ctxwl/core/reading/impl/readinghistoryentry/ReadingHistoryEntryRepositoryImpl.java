package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import com.google.common.base.Preconditions;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.reading.IncompatibleReadingHistoryEntryException;
import org.zith.expr.ctxwl.core.reading.OutdatedTimestampException;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.common.CollectionNames;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class ReadingHistoryEntryRepositoryImpl implements ReadingHistoryEntryRepository {

    private final ComponentFactory componentFactory;
    private final MongoCollection<ReadingHistoryEntryDocument> collectionOfReadHistoryEntries;

    public ReadingHistoryEntryRepositoryImpl(
            ComponentFactory componentFactory,
            MongoCollection<ReadingHistoryEntryDocument> collectionOfReadHistoryEntries
    ) {
        this.componentFactory = componentFactory;
        this.collectionOfReadHistoryEntries = collectionOfReadHistoryEntries;
    }

    public static ReadingHistoryEntryRepositoryImpl create(
            ComponentFactory componentFactory,
            MongoDatabase mongoDatabase,
            boolean reinitializeData
    ) {
        var collectionOfReadHistoryEntries =
                mongoDatabase.getCollection(CollectionNames.READING_HISTORY_ENTRIES, ReadingHistoryEntryDocument.class);
        if (reinitializeData) {
            collectionOfReadHistoryEntries.drop();
        }
        collectionOfReadHistoryEntries.createIndexes(List.of(
                new IndexModel(Indexes.ascending(
                        ReadingHistoryEntryDocument.Fields.session_group,
                        ReadingHistoryEntryDocument.Fields.session_serial,
                        ReadingHistoryEntryDocument.Fields.serial),
                        new IndexOptions().unique(true)),
                new IndexModel(Indexes.ascending(
                        ReadingHistoryEntryDocument.Fields.updateTime,
                        ReadingHistoryEntryDocument.Fields.creationTime),
                        new IndexOptions()
                                .partialFilterExpression(Filters.and(
                                        Filters.exists(ReadingHistoryEntryDocument.Fields.unprocessed),
                                        Filters.eq(ReadingHistoryEntryDocument.Fields.unprocessed, true))))));
        return new ReadingHistoryEntryRepositoryImpl(componentFactory, collectionOfReadHistoryEntries);
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> upsert(
            Session session,
            long serial,
            ReadingHistoryEntryValue value,
            @Nullable Instant timestampBarrier
    ) {
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(serial >= 0);
        Preconditions.checkArgument(value.creationTime().isPresent());
        Preconditions.checkArgument(
                value.creationTime().get().truncatedTo(ChronoUnit.MILLIS).equals(value.creationTime().get()));
        Preconditions.checkArgument(
                value.updateTime().map(t -> t.truncatedTo(ChronoUnit.MILLIS).equals(t)).orElse(true));
        if (value.updateTime().isEmpty()) {
            Preconditions.checkArgument(value.majorSerial().isEmpty());

            if (!(timestampBarrier == null || !value.creationTime().get().isBefore(timestampBarrier)))
                throw new OutdatedTimestampException();

            try {
                var document = new ReadingHistoryEntryDocument(
                        new ReadingSessionKeyDocument(session.getGroup(), session.getSerial()),
                        serial,
                        value.uri(),
                        value.text().orElse(null),
                        value.creationTime().get(),
                        null,
                        null,
                        true,
                        null
                );
                collectionOfReadHistoryEntries.insertOne(document);
                return componentFactory.createReadingHistoryEntryImpl(this, session, serial, null, document);
            } catch (MongoWriteException e) {
                if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                    return update(session, serial, value, timestampBarrier);
                }
                throw e;
            }
        } else {
            return update(session, serial, value, timestampBarrier);
        }
    }

    @NotNull
    private <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> update(
            Session session,
            long serial,
            ReadingHistoryEntryValue value,
            @Nullable Instant timestampBarrier) {
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(serial >= 0);
        Preconditions.checkArgument(value.creationTime().isPresent());
        var base = collectionOfReadHistoryEntries.find(Filters.and(
                Filters.eq(ReadingHistoryEntryDocument.Fields.session_group, session.getGroup()),
                Filters.eq(ReadingHistoryEntryDocument.Fields.session_serial, session.getSerial()),
                Filters.eq(ReadingHistoryEntryDocument.Fields.serial, serial)
        )).first();
        if (base == null) throw new NoSuchElementException();
        if (!(base.uri() == null || Objects.equals(base.uri(), value.uri())))
            throw new IncompatibleReadingHistoryEntryException();
        if (!(base.creationTime() == null || Objects.equals(base.creationTime(), value.creationTime().get())))
            throw new IncompatibleReadingHistoryEntryException();
        if (!(base.majorSerial() == null ||
                value.majorSerial().isEmpty() ||
                Objects.equals(base.majorSerial(), value.majorSerial().get())))
            throw new IncompatibleReadingHistoryEntryException();

        var document = new ReadingHistoryEntryDocument(
                base.session(),
                base.serial(),
                Optional.ofNullable(base.uri()).orElse(value.uri()),
                Optional.ofNullable(base.text()).or(value::text).orElse(null),
                Optional.ofNullable(base.creationTime()).or(value::creationTime).orElse(null),
                Optional.ofNullable(base.updateTime()).or(value::updateTime).orElse(null),
                Optional.ofNullable(base.majorSerial()).or(value::majorSerial).orElse(null),
                true,
                base.id()
        );

        if (Objects.equals(base.session(), document.session()) &&
                Objects.equals(base.serial(), document.serial()) &&
                Objects.equals(base.uri(), document.uri()) &&
                Objects.equals(base.text(), document.text()) &&
                Objects.equals(base.creationTime(), document.creationTime()) &&
                Objects.equals(base.updateTime(), document.updateTime()))
            return componentFactory.createReadingHistoryEntryImpl(this, session, serial, base.id(), document);

        if (base.creationTime() == null) {
            if (!(timestampBarrier == null || !value.creationTime().get().isBefore(timestampBarrier)))
                throw new OutdatedTimestampException();
        } else {
            Preconditions.checkArgument(value.updateTime().isPresent());
            if (!(timestampBarrier == null || !value.updateTime().get().isBefore(timestampBarrier)))
                throw new OutdatedTimestampException();

            if (!((base.updateTime() == null && document.updateTime() != null) ||
                    (base.updateTime() != null && document.updateTime() != null &&
                            base.updateTime().isBefore(document.updateTime()))))
                throw new IncompatibleReadingHistoryEntryException();
        }

        if (collectionOfReadHistoryEntries.replaceOne(
                Filters.eq(ReadingHistoryEntryDocument.Fields.id, document.id()),
                document).getMatchedCount() != 1)
            throw new IllegalStateException(); // TODO handle read / write concern

        return componentFactory.createReadingHistoryEntryImpl(this, session, serial, base.id(), document);
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> get(Session session, long serial) {
        return componentFactory.createReadingHistoryEntryImpl(this, session, serial, null, null);
    }

    @Override
    public <Session extends ReadingSession> ObjectId ensureReference(Session session, long serial) {
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(serial >= 0);
        try {
            var result = collectionOfReadHistoryEntries.insertOne(
                    new ReadingHistoryEntryDocument(
                            new ReadingSessionKeyDocument(session.getGroup(), session.getSerial()),
                            serial,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ),
                    new InsertOneOptions()
            );
            return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue();
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                var base = collectionOfReadHistoryEntries
                        .find(Filters.and(
                                Filters.eq(ReadingHistoryEntryDocument.Fields.session_group, session.getGroup()),
                                Filters.eq(ReadingHistoryEntryDocument.Fields.session_serial, session.getSerial()),
                                Filters.eq(ReadingHistoryEntryDocument.Fields.serial, serial)
                        ))
                        .projection(Projections.include(ReadingHistoryEntryDocument.Fields.id))
                        .first();
                if (base == null) throw new IllegalStateException(); // TODO handle read / write concern
                return Objects.requireNonNull(Objects.requireNonNull(base).id());
            }
            throw e;
        }
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session>
    resolveReference(SessionProvider<Session> sessionProvider, ObjectId reference) {
        return componentFactory.createReadingHistoryEntryImpl(this, sessionProvider, reference, null);
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> get(
            Session session,
            long serial,
            ObjectId reference
    ) {
        return componentFactory.createReadingHistoryEntryImpl(this, session, serial, reference, null);
    }

    @NotNull
    ReadingHistoryEntryDocument fetch(ReadingSession session, long serial) {
        var document = collectionOfReadHistoryEntries
                .find(Filters.and(
                        Filters.eq(ReadingHistoryEntryDocument.Fields.session_group, session.getGroup()),
                        Filters.eq(ReadingHistoryEntryDocument.Fields.session_serial, session.getSerial()),
                        Filters.eq(ReadingHistoryEntryDocument.Fields.serial, serial)
                ))
                .first();

        return Objects.requireNonNull(document);
    }

    @NotNull
    ReadingHistoryEntryDocument fetch(ObjectId reference) {
        var document = collectionOfReadHistoryEntries
                .find(Filters.eq(ReadingHistoryEntryDocument.Fields.id, reference))
                .first();

        return Objects.requireNonNull(document);
    }
}
