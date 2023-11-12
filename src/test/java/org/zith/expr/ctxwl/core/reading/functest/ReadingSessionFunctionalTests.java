package org.zith.expr.ctxwl.core.reading.functest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.impl.ReadingServiceTuner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadingSessionFunctionalTests extends AbstractReadingServiceFunctionalTests {
    @Test
    public void testSessionLifecycle() {
        var session = readingService().makeSession("testSessionLifecycle", "wordlist");
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
                try (var session = readingService()
                        .makeSession("testCreatingSessionWithContention", "wordlist")) {
                    assertEquals(1, session.getSerial());
                }
                phaser.arriveAndAwaitAdvance();
            }
        });
        var thread2 = new Thread(() -> {
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
            try (var session = readingService()
                    .makeSession("testCreatingSessionWithContention", "wordlist")) {
                assertEquals(0, session.getSerial());
            }
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
        try (var session = readingService().makeSession("test", "wordlist")) {
            session.createHistoryEntry(
                    0,
                    new ReadingHistoryEntryValue(
                            "http://example.com",
                            Optional.of("test content"),
                            Optional.of(Instant.now()),
                            Optional.empty(),
                            Optional.empty()));
            session.createLookup(
                    0,
                    0,
                    new ReadingInspiredLookupValue(
                            "content",
                            Optional.of(5L),
                            Optional.of(Instant.now())
                    )
            );
        }

        var completion = new CompletableFuture<Void>();
        var data = new LinkedList<ReadingEvent>();
        readingService().collect(ForkJoinPool.commonPool()).subscribe(new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Tracked<ReadingEvent> item) {
                data.add(item.value());
                item.acknowledge();
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                completion.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completion.complete(null);
            }
        });

        completion.get();

        assertEquals(1, data.size());
        assertTrue(data.getFirst() instanceof ReadingEvent.AddingReadingInspiredLookup);
        var lookup = ((ReadingEvent.AddingReadingInspiredLookup) data.getFirst()).value();
        assertEquals("content", lookup.criterion());
        assertEquals(Optional.of(5L), lookup.offset());
        assertEquals("test", lookup.session().getGroup());
        assertEquals(Optional.of("test content"), lookup.historyEntry().orElseThrow().text());
    }

    @Test
    public void testWordlistInduction() throws ExecutionException, InterruptedException {
        try (var session = readingService().makeSession("test", "wordlist")) {
            session.createHistoryEntry(
                    0,
                    new ReadingHistoryEntryValue(
                            "http://example.com",
                            Optional.of("It is better for you to eat an apple quickly"),
                            Optional.of(Instant.now()),
                            Optional.empty(),
                            Optional.empty()));
            session.createLookup(
                    0,
                    0,
                    new ReadingInspiredLookupValue(
                            "is",
                            Optional.of(3L),
                            Optional.of(Instant.now())
                    )
            );
            session.createLookup(
                    0,
                    1,
                    new ReadingInspiredLookupValue(
                            "better",
                            Optional.of(6L),
                            Optional.of(Instant.now())
                    )
            );
            session.createLookup(
                    0,
                    2,
                    new ReadingInspiredLookupValue(
                            "for",
                            Optional.of(14L),
                            Optional.of(Instant.now())
                    )
            );
            session.createLookup(
                    0,
                    3,
                    new ReadingInspiredLookupValue(
                            "quickly",
                            Optional.of(37L),
                            Optional.of(Instant.now())
                    )
            );
            session.createLookup(
                    0,
                    4,
                    new ReadingInspiredLookupValue(
                            "dazzle",
                            Optional.of(60L),
                            Optional.of(Instant.now())
                    )
            );
        }


        var completion = new CompletableFuture<Void>();
        var data = new LinkedList<ReadingEvent>();
        readingService().collect(ForkJoinPool.commonPool()).subscribe(new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Tracked<ReadingEvent> item) {
                data.add(item.value());
                item.acknowledge();
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                completion.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completion.complete(null);
            }
        });

        completion.get();

        readingService().extendWordlist(data);

        assertEquals(
                Set.of("better", "well", "good", "quickly", "be", "for", "dazzle"),
                new HashSet<>(readingService().getWordlist("wordlist").getWords()));
    }
}
