package org.zith.expr.ctxwl.webapi.base;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.common.WebApiErrorCode;

import java.util.Objects;

public class WebApiErrorCodeAdapter extends SimpleErrorCodeAdapter<WebApiErrorCode> {

    public WebApiErrorCodeAdapter(WebApiErrorCode code) {
        super(code);
    }

    @Override
    public Response.StatusType status() {
        switch (code) {
            case UNAUTHENTICATED -> {
                return Response.Status.UNAUTHORIZED;
            }
            case DATA_ERROR -> {
                return Response.Status.BAD_REQUEST;
            }
        }

        return super.status();
    }
}
