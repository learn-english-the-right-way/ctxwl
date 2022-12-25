package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class ReadingSessionException extends WebApiDataException {
    public static class UnauthorizedAccessToSessionException extends ReadingSessionException {
    }

    public static class SessionNotFoundException extends ReadingSessionException {
    }

    public static class InvalidCredentialException extends UnauthorizedAccessToSessionException {
    }
}
