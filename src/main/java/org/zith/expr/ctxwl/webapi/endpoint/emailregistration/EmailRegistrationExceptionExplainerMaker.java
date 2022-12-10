package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

import java.util.Map;

public class EmailRegistrationExceptionExplainerMaker
        extends AbstractExceptionExplainerMaker<EmailRegistrationErrorCode, EmailRegistrationException> {
    private static final Map<EmailRegistrationErrorCode, Response.StatusType>
            statusMap = Map.of(
            EmailRegistrationErrorCode.INVALID_REQUEST, Response.Status.BAD_REQUEST,
            EmailRegistrationErrorCode.UNAUTHORIZED_EMAIL_ADDRESS, Response.Status.FORBIDDEN,
            EmailRegistrationErrorCode.INVALID_CONFIRMATION_CODE, Response.Status.FORBIDDEN
    );

    @Override
    protected Response.StatusType status(EmailRegistrationErrorCode code) {
        return statusMap.getOrDefault(code, Response.Status.BAD_REQUEST);
    }
}
