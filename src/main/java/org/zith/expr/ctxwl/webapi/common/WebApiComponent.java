package org.zith.expr.ctxwl.webapi.common;

import org.zith.expr.ctxwl.webapi.error.ErrorScope;

public enum WebApiComponent implements ErrorScope {
    API("api"),
    SESSION_AUTHENTICATION("session_authentication"),
    EMAIL_REGISTRATION("email_registration"),
    READING_HISTORY("reading_history");

    private final String identifier;

    WebApiComponent(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String identifier() {
        return identifier;
    }
}
