package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.base.SimpleErrorCodeAdapter;

public class AuthenticationErrorCodeAdapter extends SimpleErrorCodeAdapter<AuthenticationErrorCode> {

    public AuthenticationErrorCodeAdapter(AuthenticationErrorCode code) {
        super(code);
    }

    @Override
    public Response.StatusType status() {
        switch (code) {
            case INVALID_REQUEST -> {
                return Response.Status.BAD_REQUEST;
            }
            case INVALID_CREDENTIAL -> {
                return Response.Status.UNAUTHORIZED;
            }
            case UNSUPPORTED_AUTHENTICATION_METHOD -> {
                return Response.Status.NOT_FOUND;
            }
        }

        return super.status();
    }
}
