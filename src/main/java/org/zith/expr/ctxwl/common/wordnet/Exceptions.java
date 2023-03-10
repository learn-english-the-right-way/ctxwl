package org.zith.expr.ctxwl.common.wordnet;

import java.util.ArrayList;

interface Exceptions {
    static void rethrow(ArrayList<Throwable> throwables) {
        if (!throwables.isEmpty()) {
            Throwable pending = null;
            var iterator = throwables.listIterator(throwables.size());
            while (iterator.hasPrevious()) {
                var current = iterator.previous();
                if (pending == null) {
                    pending = current;
                } else {
                    pending.addSuppressed(current);
                }
            }
        }
    }
}
