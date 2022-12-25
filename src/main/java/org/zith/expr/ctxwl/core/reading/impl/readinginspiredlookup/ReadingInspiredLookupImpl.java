package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingSession;

import java.time.Instant;
import java.util.Optional;

public class ReadingInspiredLookupImpl<Session extends ReadingSession> implements BoundReadingInspiredLookup<Session> {
    private final ReadingInspiredLookupRepositoryImpl repository;
    private final Session session;
    private final long historyEntrySerial;
    private final long serial;
    private boolean historyEntryPresence;
    private @Nullable ReadingHistoryEntry historyEntry;
    private @Nullable ReadingInspiredLookupDocument document;

    private ReadingInspiredLookupImpl(
            ReadingInspiredLookupRepositoryImpl repository,
            Session session,
            long historyEntrySerial,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        this.repository = repository;
        this.session = session;
        this.historyEntrySerial = historyEntrySerial;
        this.serial = serial;
        this.document = document;
    }

    public static <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> create(
            ReadingInspiredLookupRepositoryImpl repository,
            Session session,
            long historyEntrySerial,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        return new ReadingInspiredLookupImpl<>(repository, session, historyEntrySerial, serial, document);
    }

    @Override
    public ReadingSession session() {
        return session;
    }

    @Override
    public Optional<ReadingHistoryEntry> historyEntry() {
        if (!historyEntryPresence) {
            if (document == null) {
                historyEntry = repository.readingHistoryEntryRepository()
                        .get(session, historyEntrySerial);
            } else {
                historyEntry = repository.readingHistoryEntryRepository()
                        .get(session, historyEntrySerial, document.id().parent());
            }
            historyEntryPresence = true;
        }
        return Optional.ofNullable(historyEntry);
    }

    @Override
    public long serial() {
        return serial;
    }

    @Override
    public long historyEntrySerial() {
        return historyEntrySerial;
    }

    private ReadingInspiredLookupDocument document() {
        if (document == null) {
            document = repository.fetch(session, historyEntrySerial, serial);
        }
        return document;
    }

    @Override
    public String criterion() {
        return document().criterion();
    }

    @Override
    public Optional<Long> offset() {
        return Optional.ofNullable(document().offset());
    }

    @Override
    public Optional<Instant> creationTime() {
        return Optional.ofNullable(document().creationTime());
    }
}
