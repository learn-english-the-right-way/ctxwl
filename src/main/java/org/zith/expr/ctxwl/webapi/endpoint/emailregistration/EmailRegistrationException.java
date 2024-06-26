package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class EmailRegistrationException extends WebApiDataException {
    public static class InvalidConfirmationCodeException extends EmailRegistrationException {
    }

    public static class UnauthorizedEmailAddressException extends EmailRegistrationException {
    }
}
