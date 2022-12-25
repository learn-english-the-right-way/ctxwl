package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.time.Instant;
import java.util.Optional;

public class ReadingHistoryEntryImpl<Session extends ReadingSession> implements BoundReadingHistoryEntry<Session> {
    private final ReadingHistoryEntryRepositoryImpl repository;
    private final Session session;
    private final long serial;
    private @Nullable ObjectId reference;
    private @Nullable ReadingHistoryEntryDocument document;

    private ReadingHistoryEntryImpl(
            ReadingHistoryEntryRepositoryImpl repository,
            Session session,
            long serial,
            @Nullable ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        this.repository = repository;
        this.session = session;
        this.serial = serial;
        this.reference = reference;
        this.document = document;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public long serial() {
        return serial;
    }

    private ReadingHistoryEntryDocument document() {
        if (document == null) {
            if (reference == null) {
                document = repository.fetch(session, serial);
                reference = document.id();
            } else {
                document = repository.fetch(reference);
            }
        }
        return document;
    }

    @Override
    public void set(ReadingHistoryEntryValue value) {
        new ReadingHistoryEntryDocument(
                new ReadingSessionKeyDocument(session.getGroup(), session.getSerial()),
                serial,
                value.uri(),
                value.text().orElse(null),
                value.creationTime().orElse(null),
                value.updateTime().orElse(null),
                value.majorSerial().orElse(null),
                null); // TODO
    }

    @Override
    public String uri() {
        return document().uri();
    }

    @Override
    public Optional<String> text() {
        return Optional.ofNullable(document().text());
    }

    @Override
    public Optional<Instant> creationTime() {
        return Optional.ofNullable(document().creationTime());
    }

    @Override
    public Optional<Instant> updateTime() {
        return Optional.ofNullable(document().updateTime());
    }

    @Override
    public Optional<Long> majorSerial() {
        return Optional.ofNullable(document().majorSerial());
    }

    public static <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> create(
            ReadingHistoryEntryRepositoryImpl repository,
            Session session,
            long serial,
            @Nullable ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        return new ReadingHistoryEntryImpl<>(repository, session, serial, reference, document);
    }
}
