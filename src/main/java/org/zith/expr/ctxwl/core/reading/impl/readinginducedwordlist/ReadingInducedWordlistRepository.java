package org.zith.expr.ctxwl.core.reading.impl.readinginducedwordlist;

import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingInducedWordlist;

import java.util.List;

public interface ReadingInducedWordlistRepository {
    ReadingInducedWordlist getWordlist(String id);

    void consume(List<ReadingEvent> events);
}
