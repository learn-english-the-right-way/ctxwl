package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry.BoundReadingHistoryEntry;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class ReadingInspiredLookupImpl<Session extends ReadingSession> implements BoundReadingInspiredLookup<Session> {
    private final ReadingInspiredLookupRepositoryImpl repository;
    private final long serial;
    private volatile @Nullable Session session;
    private volatile boolean historyEntryPresence;
    private volatile @Nullable ReadingInspiredLookupDocument document;
    private @Nullable Long historyEntrySerial;
    private @Nullable BoundReadingHistoryEntry<Session> historyEntry;

    private ReadingInspiredLookupImpl(
            ReadingInspiredLookupRepositoryImpl repository,
            @Nullable Session session,
            @Nullable Long historyEntrySerial,
            @Nullable BoundReadingHistoryEntry<Session> historyEntry,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        assert (historyEntrySerial == null) != (historyEntry == null);
        this.repository = repository;
        this.serial = serial;
        this.session = session;
        this.historyEntryPresence = historyEntrySerial == null;
        this.document = document;
        this.historyEntrySerial = historyEntrySerial;
        this.historyEntry = historyEntry;
    }

    public static <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> create(
            ReadingInspiredLookupRepositoryImpl repository,
            Session session,
            long historyEntrySerial,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(session);
        return new ReadingInspiredLookupImpl<>(repository, session, historyEntrySerial, null, serial, document);
    }

    public static <Session extends ReadingSession> ReadingInspiredLookupImpl<Session> create(
            ReadingInspiredLookupRepositoryImpl repository,
            BoundReadingHistoryEntry<Session> historyEntry,
            long serial,
            @Nullable ReadingInspiredLookupDocument document
    ) {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(historyEntry);
        return new ReadingInspiredLookupImpl<>(
                repository, null, null, historyEntry, serial, document);
    }

    @Override
    public Session session() {
        var session = this.session;
        if (session == null) {
            synchronized (this) {
                session = this.session;
                if (session == null) {
                    assert historyEntry != null;
                    this.session = session = historyEntry.session();
                }
            }
        }
        return session;
    }

    @Override
    public Optional<ReadingHistoryEntry> historyEntry() {
        if (!historyEntryPresence) {
            assert historyEntrySerial != null;
            synchronized (this) {
                if (!historyEntryPresence) {
                    var document = this.document;
                    if (document == null) {
                        historyEntry = repository.readingHistoryEntryRepository()
                                .get(session, historyEntrySerial);
                    } else {
                        historyEntry = repository.readingHistoryEntryRepository()
                                .get(session, historyEntrySerial, document.id().parent());
                    }
                    historyEntryPresence = true;
                }
            }
        }
        return Optional.ofNullable(historyEntry);
    }

    @Override
    public long serial() {
        return serial;
    }

    @Override
    public long historyEntrySerial() {
        return Objects.requireNonNullElseGet(
                historyEntrySerial,
                () -> {
                    assert historyEntry != null;
                    return historyEntrySerial = historyEntry.serial();
                }
        );
    }

    private ReadingInspiredLookupDocument document() {
        if (document == null) {
            synchronized (this) {
                if (document == null) {
                    document = repository.fetch(session, historyEntrySerial(), serial);
                }
            }
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
