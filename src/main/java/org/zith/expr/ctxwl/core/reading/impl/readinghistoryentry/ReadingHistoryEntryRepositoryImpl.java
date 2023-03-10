package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import com.google.common.base.Preconditions;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.common.CollectionNames;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.util.List;
import java.util.Objects;

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

    public void create(ReadingHistoryEntryDocument document) {
        collectionOfReadHistoryEntries.insertOne(document);
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
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> create(
            Session session,
            long serial,
            ReadingHistoryEntryValue value
    ) {
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(serial >= 0);
        Preconditions.checkArgument(value.creationTime().isPresent()); // TODO check if the time is recent
        Preconditions.checkArgument(value.updateTime().isEmpty());
        Preconditions.checkArgument(value.majorSerial().isEmpty());
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
        collectionOfReadHistoryEntries.insertOne(document); // TODO handle conflicts
        return componentFactory.createReadingHistoryEntryImpl(this, session, serial, null, document);
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
                var document = collectionOfReadHistoryEntries
                        .find(Filters.and(
                                Filters.eq(ReadingHistoryEntryDocument.Fields.session_group, session.getGroup()),
                                Filters.eq(ReadingHistoryEntryDocument.Fields.session_serial, session.getSerial()),
                                Filters.eq(ReadingHistoryEntryDocument.Fields.serial, serial)
                        ))
                        .projection(Projections.include(ReadingHistoryEntryDocument.Fields.id))
                        .first();
                return Objects.requireNonNull(Objects.requireNonNull(document).id());
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
