package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import org.zith.expr.ctxwl.webapi.error.ErrorCode;
import org.zith.expr.ctxwl.webapi.common.WebApiComponent;

public enum EmailRegistrationErrorCode implements ErrorCode {
    INVALID_REQUEST("invalid_request"),
    UNAUTHORIZED_EMAIL_ADDRESS("unauthorized_email_address"),
    INVALID_CONFIRMATION_CODE("invalid_confirmation_code");

    private final String representation;

    EmailRegistrationErrorCode(String representation) {
        this.representation = representation;
    }

    @Override
    public WebApiComponent component() {
        return WebApiComponent.EMAIL_REGISTRATION;
    }

    @Override
    public String representation() {
        return representation;
    }
}
