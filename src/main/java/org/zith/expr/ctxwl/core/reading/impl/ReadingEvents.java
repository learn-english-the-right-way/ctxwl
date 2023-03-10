package org.zith.expr.ctxwl.core.reading.impl;

import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookup;

public interface ReadingEvents {
    record AddingReadingInspiredLookup(
            ReadingInspiredLookup value)
            implements ReadingEvent.AddingReadingInspiredLookup {
    }
}
