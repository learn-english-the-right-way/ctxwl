package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import org.zith.expr.ctxwl.webapi.common.WebApiComponent;
import org.zith.expr.ctxwl.webapi.error.ErrorCode;

public enum ReadingSessionErrorCode implements ErrorCode {
    INVALID_REQUEST("invalid_request"),
    SESSION_ACCESS_NOT_AUTHORIZED("session_access_not_authorized"),
    SESSION_NOT_FOUND("session_not_found"),
    INVALID_CREDENTIAL("invalid_credential");

    private final String representation;

    ReadingSessionErrorCode(String representation) {
        this.representation = representation;
    }

    @Override
    public WebApiComponent component() {
        return WebApiComponent.READING_SESSION;
    }

    @Override
    public String representation() {
        return representation;
    }
}
