package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.util.List;

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

    public static ReadingHistoryEntryRepositoryImpl create(ComponentFactory componentFactory, MongoDatabase mongoDatabase) {
        var collectionOfReadHistoryEntries =
                mongoDatabase.getCollection("read_history_entries", ReadingHistoryEntryDocument.class)
                        .withCodecRegistry(CodecRegistries.fromProviders(
                                PojoCodecProvider.builder().register(ReadingHistoryEntryDocument.class).build(),
                                mongoDatabase.getCodecRegistry()));
        collectionOfReadHistoryEntries.createIndexes(
                List.of(new IndexModel(Indexes.ascending(
                        ReadingHistoryEntryDocument.Fields.session_group,
                        ReadingHistoryEntryDocument.Fields.session_serial,
                        ReadingHistoryEntryDocument.Fields.serial),
                        new IndexOptions().unique(true))));
        return new ReadingHistoryEntryRepositoryImpl(componentFactory, collectionOfReadHistoryEntries);
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> create(
            Session session,
            long serial,
            ReadingHistoryEntryValue value
    ) {
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
                null
        );
        collectionOfReadHistoryEntries.insertOne(document); // TODO handle conflicts
        return componentFactory.createReadingHistoryEntryImpl(this, session, serial, document);
    }

    @Override
    public <Session extends ReadingSession> BoundReadingHistoryEntry<Session> get(Session session, long serial) {
        return null; // TODO
    }

    public ReadingHistoryEntryDocument fetch(String sessionGroup, long sessionSerial, long serial) {
        return null; // TODO
    }
}
