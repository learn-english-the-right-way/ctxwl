package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

import java.util.Map;

public class AuthenticationExceptionExplainerMaker
        extends AbstractExceptionExplainerMaker<AuthenticationErrorCode, AuthenticationException> {
    private static final Map<AuthenticationErrorCode, Response.StatusType>
            statusMap = Map.of(
            AuthenticationErrorCode.INVALID_REQUEST, Response.Status.BAD_REQUEST,
            AuthenticationErrorCode.INVALID_CREDENTIAL, Response.Status.UNAUTHORIZED,
            AuthenticationErrorCode.UNSUPPORTED_AUTHENTICATION_METHOD, Response.Status.NOT_FOUND
    );

    @Override
    protected Response.StatusType status(AuthenticationErrorCode code) {
        return statusMap.getOrDefault(code, Response.Status.BAD_REQUEST);
    }
}
