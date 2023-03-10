package org.zith.expr.ctxwl.common.async;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class Strand implements Executor {
    private final Executor executor;
    private final LinkedList<Runnable> pending = new LinkedList<>();
    private boolean executing;

    public Strand(Executor executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executor.execute(() -> {
            synchronized (this) {
                if (executing) {
                    pending.add(command);
                    return;
                }
                executing = true;
            }

            processAndProceed(command);
        });
    }

    private void processAndProceed(Runnable command) {
        boolean attemptedToProceed = false;
        try {
            command.run();

            attemptedToProceed = true;
            proceed();
        } catch (Exception exception) {
            try {
                if (!attemptedToProceed) proceed();
            } catch (Throwable throwable) {
                synchronized (this) {
                    executing = false;
                }
                throwable.addSuppressed(exception);
                throw throwable;
            }
            throw exception;
        } catch (Throwable throwable) {
            synchronized (this) {
                executing = false;
            }
            throw throwable;
        }
    }

    private void proceed() {
        Runnable next;
        synchronized (this) {
            next = pending.poll();
            if (next == null) {
                executing = false;
                return;
            }
        }

        executor.execute(() -> processAndProceed(next));
    }
}
