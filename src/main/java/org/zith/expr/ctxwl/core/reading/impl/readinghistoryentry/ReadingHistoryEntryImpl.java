package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.core.reading.impl.common.SessionProvider;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class ReadingHistoryEntryImpl<Session extends ReadingSession> implements BoundReadingHistoryEntry<Session> {
    private final ReadingHistoryEntryRepositoryImpl repository;
    private final @Nullable SessionProvider<Session> sessionProvider;
    private volatile @Nullable Session session;
    private volatile @Nullable Long serial;
    private @Nullable ObjectId reference;
    private volatile @Nullable ReadingHistoryEntryDocument document;

    private ReadingHistoryEntryImpl(
            ReadingHistoryEntryRepositoryImpl repository,
            @Nullable SessionProvider<Session> sessionProvider,
            @Nullable Session session,
            @Nullable Long serial,
            @Nullable ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        this.repository = repository;
        this.session = session;
        this.sessionProvider = sessionProvider;
        this.serial = serial;
        this.reference = reference;
        this.document = document;
    }

    @Override
    public Session session() {
        var session = this.session;
        if (session == null) {
            synchronized (this) {
                session = this.session;
                if (session == null) {
                    assert sessionProvider != null;
                    var sessionDocument = document().session();
                    this.session = session =
                            sessionProvider.getSession(sessionDocument.group(), sessionDocument.serial());
                }
            }
        }
        return session;
    }

    @Override
    public long serial() {
        var serial = this.serial;
        if (serial == null) {
            synchronized (this) {
                serial = this.serial;
                if (serial == null) {
                    this.serial = serial = document().serial();
                }
            }
        }
        return serial;
    }

    @NotNull
    private ReadingHistoryEntryDocument document() {
        var document = this.document;
        if (document == null) {
            synchronized (this) {
                document = this.document;
                if (document == null) {
                    if (reference == null) {
                        var serial = this.serial;
                        var session = this.session;
                        assert session != null;
                        assert serial != null;
                        this.document = document = repository.fetch(session, serial);
                        reference = document.id();
                    } else {
                        this.document = document = repository.fetch(reference);
                    }
                }
            }
        }
        return document;
    }

    @Override
    public void set(ReadingHistoryEntryValue value) {
        throw new UnsupportedOperationException(); // TODO do update
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
        Objects.requireNonNull(repository);
        Objects.requireNonNull(session);
        return new ReadingHistoryEntryImpl<>(repository, null, session, serial, reference, document);
    }

    public static <Session extends ReadingSession> ReadingHistoryEntryImpl<Session> create(
            ReadingHistoryEntryRepositoryImpl repository,
            SessionProvider<Session> sessionProvider,
            ObjectId reference,
            @Nullable ReadingHistoryEntryDocument document
    ) {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(sessionProvider);
        Objects.requireNonNull(reference);
        return new ReadingHistoryEntryImpl<>(repository, sessionProvider, null, null, reference, document);
    }
}
