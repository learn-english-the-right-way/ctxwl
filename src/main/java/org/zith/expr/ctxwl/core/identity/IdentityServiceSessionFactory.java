package org.zith.expr.ctxwl.core.identity;

public interface IdentityServiceSessionFactory extends AutoCloseable {
    IdentityServiceSession openSession();
}
