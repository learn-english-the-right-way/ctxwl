package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.base.SimpleErrorCodeAdapter;
import org.zith.expr.ctxwl.webapi.endpoint.authentication.AuthenticationErrorCode;

public class EmailRegistrationErrorCodeAdapter extends SimpleErrorCodeAdapter<EmailRegistrationErrorCode> {

    public EmailRegistrationErrorCodeAdapter(EmailRegistrationErrorCode code) {
        super(code);
    }

    @Override
    public Response.StatusType status() {
        switch (code) {
            case INVALID_REQUEST -> {
                return Response.Status.BAD_REQUEST;
            }
            case UNAUTHORIZED_EMAIL_ADDRESS, INVALID_CONFIRMATION_CODE -> {
                return Response.Status.FORBIDDEN;
            }
        }

        return super.status();
    }
}
