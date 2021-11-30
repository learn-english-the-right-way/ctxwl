package org.zith.expr.ctxwl.core.reading;

public interface ReadingHistoryEntry extends ReadingHistoryEntryValueLike {
    ReadingSession session();

    long serial();

    void set(ReadingHistoryEntryValue value);
}