package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.zith.expr.ctxwl.common.async.Strand;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.reading.impl.common.CollectionNames;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.ReadingHistoryEntryRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

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
                readingInspiredLookupValue.creationTime().orElse(null),
                true
        );
        collectionOfReadingInspiredLookups.insertOne(document); // TODO handle conflicts
        return componentFactory.createReadingInspiredLookupImpl(this, session, historyEntrySerial, serial, document);
    }

    @Override
    public <Session extends ReadingSession> Flow.Publisher<Tracked<BoundReadingInspiredLookup<Session>>>
    collect(Executor executor, SessionProvider<Session> sessionProvider) {
        var documents = collectionOfReadingInspiredLookups
                .find(Filters.eq(ReadingInspiredLookupDocument.Fields.unprocessed, true))
                .sort(Sorts.ascending(ReadingInspiredLookupDocument.Fields.creationTime))
                .partial(true);
        var acknowledgements = new ConcurrentLinkedQueue<ReadingInspiredLookupDocument.Id>();

        class Shipper {
            private final Flow.Subscriber<? super Tracked<BoundReadingInspiredLookup<Session>>> subscriber;
            private MongoCursor<ReadingInspiredLookupDocument> iterator;
            private boolean cancelled;

            public Shipper(Flow.Subscriber<? super Tracked<BoundReadingInspiredLookup<Session>>> subscriber) {
                this.subscriber = subscriber;
                this.iterator = null;
                this.cancelled = false;
            }

            private synchronized void ship() {
                if (cancelled) return;

                if (iterator == null) iterator = documents.iterator();

                if (!iterator.hasNext()) {
                    iterator.close();
                    subscriber.onComplete();
                    return;
                }

                ReadingInspiredLookupDocument.Id id;
                ReadingInspiredLookupImpl<Session> lookup;
                try {
                    var document = iterator.next();
                    id = document.id();
                    var parent = readingHistoryEntryRepository.resolveReference(
                            sessionProvider, document.historyEntryReference());
                    lookup = componentFactory.createReadingInspiredLookupImpl(
                            ReadingInspiredLookupRepositoryImpl.this, parent, document.serial(), document);
                } catch (Throwable th) {
                    cancelled = true;
                    iterator.close();
                    subscriber.onError(th);
                    if (!(th instanceof Exception)) {
                        throw th;
                    }
                    return;
                }
                subscriber.onNext(Tracked.of(lookup, () -> {
                    acknowledgements.add(id);
                    executor.execute(Shipper.this::processAcknowledgements);
                }));
            }

            public synchronized void cancel() {
                if (iterator == null) return;
                iterator.close();
                cancelled = true;
            }

            public void processAcknowledgements() {
                boolean restart;
                do {
                    restart = false;
                    var ids = new LinkedList<ReadingInspiredLookupDocument.Id>();
                    for (var id = acknowledgements.poll(); id != null; id = acknowledgements.poll()) {
                        ids.add(id);
                        if (ids.size() >= 0x40) {
                            restart = true;
                            break;
                        }
                    }
                    if (!ids.isEmpty()) {
                        try {
                            collectionOfReadingInspiredLookups.updateMany(
                                    Filters.in(ReadingInspiredLookupDocument.Fields.id, ids),
                                    Updates.unset(ReadingInspiredLookupDocument.Fields.unprocessed)
                            );
                        } catch (Exception e) {
                            acknowledgements.addAll(ids);
                            throw e;
                        }
                    }
                } while (restart);
            }
        }

        return subscriber -> {
            AtomicBoolean cancelled = new AtomicBoolean(false);
            Shipper shipper = new Shipper(subscriber);
            Strand strand = new Strand(executor);

            var subscription = new Flow.Subscription() {
                @Override
                public void request(long n) {
                    strand.execute(() -> {
                        for (long i = 0; i < n; i++) {
                            if (cancelled.getAcquire()) {
                                shipper.cancel();
                                break;
                            }
                            shipper.ship();

                            if (acknowledgements.size() > 0x80) shipper.processAcknowledgements();
                        }
                        shipper.processAcknowledgements();
                    });
                }

                @Override
                public void cancel() {
                    cancelled.lazySet(true);
                    strand.execute(shipper::cancel);
                }
            };

            strand.execute(() -> subscriber.onSubscribe(subscription));
        };
    }

    public static ReadingInspiredLookupRepositoryImpl create(
            ComponentFactory componentFactory,
            ReadingHistoryEntryRepository readingHistoryEntryRepository,
            MongoDatabase mongoDatabase,
            boolean reinitializeData
    ) {
        var collectionOfReadingInspiredLookups =
                mongoDatabase.getCollection(CollectionNames.READING_INSPIRED_LOOKUPS, ReadingInspiredLookupDocument.class);
        if (reinitializeData) {
            collectionOfReadingInspiredLookups.drop();
        }
        collectionOfReadingInspiredLookups.createIndexes(List.of(
                new IndexModel(Indexes.ascending(
                        ReadingInspiredLookupDocument.Fields.creationTime),
                        new IndexOptions()
                                .partialFilterExpression(Filters.and(
                                        Filters.exists(ReadingInspiredLookupDocument.Fields.unprocessed),
                                        Filters.eq(ReadingInspiredLookupDocument.Fields.unprocessed, true))))));
        return new ReadingInspiredLookupRepositoryImpl(
                componentFactory,
                readingHistoryEntryRepository,
                collectionOfReadingInspiredLookups
        );
    }

    ReadingHistoryEntryRepository readingHistoryEntryRepository() {
        return readingHistoryEntryRepository;
    }

    ReadingInspiredLookupDocument fetch(ReadingSession session, long historyEntrySerial, long serial) {
        var reference = readingHistoryEntryRepository.ensureReference(session, historyEntrySerial);
        return Objects.requireNonNull(
                collectionOfReadingInspiredLookups
                        .find(Filters.eq(
                                ReadingInspiredLookupDocument.Fields.id,
                                new ReadingInspiredLookupDocument.Id(reference, serial)
                        ))
                        .first());
    }
}
