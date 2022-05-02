package org.zith.expr.ctxwl.webapi.common;

public enum WebApiErrorCode implements ErrorCode {
    UNAUTHENTICATED("unauthenticated"),
    DATA_ERROR("data_error");

    private final String representation;

    WebApiErrorCode(String representation) {
        this.representation = representation;
    }

    @Override
    public WebApiComponent component() {
        return WebApiComponent.API;
    }

    @Override
    public String representation() {
        return representation;
    }
}
