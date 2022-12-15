package org.zith.expr.ctxwl.core.reading.functest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.impl.ReadingServiceTuner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Phaser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadingSessionFunctionalTests extends AbstractReadingServiceFunctionalTests {
    @Test
    public void testSessionLifecycle() {
        var session = readingService().makeSession("testSessionLifecycle");
        var completionTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        session.complete(completionTime);
        session.complete(completionTime);
    }

    @Test
    public void testCreatingSessionWithContention() throws Exception {
        var phaser = new Phaser();
        phaser.bulkRegister(3);
        var thread1 = new Thread(() -> {
            phaser.arriveAndAwaitAdvance();
            try (var tuner = new ReadingServiceTuner()) {
                tuner.tune(readingService());
                tuner.onSessionInsertion(entity -> {
                    phaser.arriveAndAwaitAdvance();
                    phaser.arriveAndAwaitAdvance();
                });
                var session = readingService().makeSession("testCreatingSessionWithContention");
                assertEquals(1, session.getSerial());
                phaser.arriveAndAwaitAdvance();
            }
        });
        var thread2 = new Thread(() -> {
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
            var session = readingService().makeSession("testCreatingSessionWithContention");
            assertEquals(0, session.getSerial());
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
        });
        thread1.start();
        thread2.start();
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        thread1.join();
        thread2.join();
    }

    @Test
    public void testCreatingHistoryEntry() throws Exception {
        var session = readingService().makeSession("test");
        session.create(
                0,
                new ReadingHistoryEntryValue(
                        "http://example.com",
                        Optional.of("test content"),
                        Optional.of(Instant.now()),
                        Optional.empty(),
                        Optional.empty()));
    }
}
