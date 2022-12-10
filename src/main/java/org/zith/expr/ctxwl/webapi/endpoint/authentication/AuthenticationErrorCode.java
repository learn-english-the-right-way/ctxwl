package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import org.zith.expr.ctxwl.webapi.error.ErrorCode;
import org.zith.expr.ctxwl.webapi.common.WebApiComponent;

public enum AuthenticationErrorCode implements ErrorCode {
    INVALID_REQUEST("invalid_request"),
    INVALID_CREDENTIAL("invalid_credential"),
    UNSUPPORTED_AUTHENTICATION_METHOD("unsupported_authentication_method");

    private final String representation;

    AuthenticationErrorCode(String representation) {
        this.representation = representation;
    }

    @Override
    public WebApiComponent component() {
        return WebApiComponent.SESSION_AUTHENTICATION;
    }

    @Override
    public String representation() {
        return representation;
    }
}
