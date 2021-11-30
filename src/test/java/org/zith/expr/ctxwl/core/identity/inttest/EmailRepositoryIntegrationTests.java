package org.zith.expr.ctxwl.core.identity.inttest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailRepositoryTuner;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailRepositoryIntegrationTests extends AbstractIdentityServiceIntegrationTests {
    @Test
    public void testEnsuringEmailConcurrently() throws Exception {
        var phaser = new Phaser();
        var address = "test@example.com";
        phaser.bulkRegister(3);
        var thread1 = new Thread(() -> {
            phaser.arriveAndAwaitAdvance();
            try (var session = identityService().openSession()) {
                var insertionCount = new AtomicInteger(0);
                var completionCount = new AtomicInteger(0);
                session.withTransaction(() -> {
                    try (var tuner = new EmailRepositoryTuner()) {
                        assertTrue(tuner.intercept(session.emailRepository()));
                        if (insertionCount.getAndIncrement() == 0) {
                            tuner.onInsertion((entity) -> {
                                assertEquals(address, entity.getAddress());
                                phaser.arriveAndAwaitAdvance();
                            });
                        }
                        session.emailRepository().ensure(address);
                        if (completionCount.getAndIncrement() == 0) {
                            phaser.arriveAndAwaitAdvance();
                        }
                    }
                    return null;
                });
            }
            phaser.arriveAndAwaitAdvance();
        });
        var thread2 = new Thread(() -> {
            phaser.arriveAndAwaitAdvance();
            try (var session = identityService().openSession()) {
                var completionCount = new AtomicInteger(0);
                session.withTransaction(() -> {
                    try (var tuner = new EmailRepositoryTuner()) {
                        assertTrue(tuner.intercept(session.emailRepository()));
                        session.emailRepository().ensure(address);
                        if (completionCount.getAndIncrement() == 0) {
                            phaser.arriveAndAwaitAdvance();
                        }
                    }
                    return null;
                });
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
}
