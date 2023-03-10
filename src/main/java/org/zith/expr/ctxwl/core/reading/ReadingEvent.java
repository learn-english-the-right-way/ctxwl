package org.zith.expr.ctxwl.core.reading;

public sealed interface ReadingEvent {
    non-sealed interface AddingReadingInspiredLookup extends ReadingEvent {
        ReadingInspiredLookup value();
    }
}
