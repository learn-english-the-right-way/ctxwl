package org.zith.expr.ctxwl.webapi.error;

public interface ErrorCode {
    ErrorScope component();

    String representation();
}
