package org.zith.expr.ctxwl.webapi.common;

public enum WebApiComponent {
    API("api"),
    SESSION_AUTHENTICATION("session_authentication"),
    EMAIL_REGISTRATION("email_registration");

    private final String identifier;

    WebApiComponent(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
    }
}
