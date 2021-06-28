package org.zith.expr.ctxwl.core.identity;

public interface EmailRepository {
    Email ensure(String address);
}
