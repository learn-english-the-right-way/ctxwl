package org.zith.expr.ctxwl.common.close;

import java.util.ArrayList;

public final class CombinedAutoCloseable implements AutoCloseable {
    private final ArrayList<AutoCloseable> resources;

    private CombinedAutoCloseable() {
        this(new ArrayList<>());
    }

    private CombinedAutoCloseable(ArrayList<AutoCloseable> resources) {
        this.resources = resources;
    }

    public synchronized <T extends AutoCloseable> T register(T closeable) {
        resources.add(closeable);
        return closeable;
    }

    public synchronized CombinedAutoCloseable transfer() {
        var result = new CombinedAutoCloseable(new ArrayList<>(resources));
        resources.clear();
        return result;
    }

    @Override
    public synchronized void close() {
        var throwables = new ArrayList<Throwable>(resources.size());

        var iterator = resources.listIterator(resources.size());
        while (iterator.hasPrevious()) {
            var closeable = iterator.previous();
            try {
                closeable.close();
            } catch (Exception e) {
                throwables.add(e);
            }
        }

        if (throwables.size() == 1) {
            var throwable = throwables.get(0);
            if (throwable instanceof RuntimeException e) {
                throw e;
            } else {
                throw new RuntimeException(throwable);
            }
        } else if (throwables.size() > 1) {
            var combined = new RuntimeException();
            throwables.forEach(combined::addSuppressed);
            throw combined;
        }

        resources.clear();
    }

    public static CombinedAutoCloseable create() {
        return new CombinedAutoCloseable();
    }
}
