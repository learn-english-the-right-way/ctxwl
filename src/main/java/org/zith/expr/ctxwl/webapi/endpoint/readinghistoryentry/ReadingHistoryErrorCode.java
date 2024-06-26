package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import org.zith.expr.ctxwl.webapi.common.WebApiComponent;
import org.zith.expr.ctxwl.webapi.error.ErrorCode;

public enum ReadingHistoryErrorCode implements ErrorCode {
    INVALID_REQUEST("invalid_request"),
    FIELD_NOT_ACCEPTED("field_not_accepted");

    private final String representation;

    ReadingHistoryErrorCode(String representation) {
        this.representation = representation;
    }

    @Override
    public WebApiComponent component() {
        return WebApiComponent.READING_HISTORY;
    }

    @Override
    public String representation() {
        return representation;
    }
}
