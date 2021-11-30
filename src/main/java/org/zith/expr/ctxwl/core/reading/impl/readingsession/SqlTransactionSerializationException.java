package org.zith.expr.ctxwl.core.reading.impl.readingsession;

public class SqlTransactionSerializationException extends RuntimeException {
    public SqlTransactionSerializationException() {
    }

    public SqlTransactionSerializationException(String message) {
        super(message);
    }

    public SqlTransactionSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlTransactionSerializationException(Throwable cause) {
        super(cause);
    }
}
