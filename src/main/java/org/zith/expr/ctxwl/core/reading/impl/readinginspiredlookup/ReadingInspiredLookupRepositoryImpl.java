package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;

public class ReadingInspiredLookupRepositoryImpl implements ReadingInspiredLookupRepository {

    private final ComponentFactory componentFactory;
    private final ReadingHistoryEntryRepository readingHistoryEntryRepository;
    private final MongoCollection<ReadingInspiredLookupDocument> collectionOfReadingInspiredLookups;

    public ReadingInspiredLookupRepositoryImpl(
            ComponentFactory componentFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            MongoCollection<ReadingInspiredLookupDocument> collectionOfReadingInspiredLookups
    ) {
        this.componentFactory = componentFactory;
        this.readingHistoryEntryRepository = readingHistoryEntryRepository;
        this.collectionOfReadingInspiredLookups = collectionOfReadingInspiredLookups;
    }

    @Override
    public <Session extends ReadingSession> BoundReadingInspiredLookup<Session> create(
            Session session,
            long historyEntrySerial,
            long serial,
            ReadingInspiredLookupValue readingInspiredLookupValue
    ) {
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(serial >= 0);
        var parent = readingHistoryEntryRepository.ensureReference(session, historyEntrySerial);
        var document = new ReadingInspiredLookupDocument(
                new ReadingInspiredLookupDocument.Id(
                        parent,
                        serial
                ),
                readingInspiredLookupValue.criterion(),
                readingInspiredLookupValue.offset().orElse(null),
                readingInspiredLookupValue.creationTime().orElse(null)
        );
        collectionOfReadingInspiredLookups.insertOne(document); // TODO handle conflicts
        return componentFactory.createReadingInspiredLookupImpl();
    }

    public static ReadingInspiredLookupRepositoryImpl create(
            ComponentFactory componentFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            MongoDatabase mongoDatabase,
            boolean reinitializeData
    ) {
        var collectionOfReadingInspiredLookups =
                mongoDatabase.getCollection("readingInspiredLookups", ReadingInspiredLookupDocument.class);
        if (reinitializeData) {
            collectionOfReadingInspiredLookups.drop();
        }
        return new ReadingInspiredLookupRepositoryImpl(
                componentFactory,
                readingHistoryEntryRepository,
                collectionOfReadingInspiredLookups
        );
    }
}
