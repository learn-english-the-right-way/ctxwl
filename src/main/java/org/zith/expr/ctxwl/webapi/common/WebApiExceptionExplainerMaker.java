package org.zith.expr.ctxwl.webapi.common;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

public class WebApiExceptionExplainerMaker extends AbstractExceptionExplainerMaker<WebApiErrorCode, WebApiException> {
    @Override
    protected Response.StatusType status(WebApiErrorCode code) {
        switch (code) {
            case UNAUTHENTICATED -> {
                return Response.Status.UNAUTHORIZED;
            }
            case DATA_ERROR -> {
                return Response.Status.BAD_REQUEST;
            }
        }

        return Response.Status.BAD_REQUEST;
    }
}
