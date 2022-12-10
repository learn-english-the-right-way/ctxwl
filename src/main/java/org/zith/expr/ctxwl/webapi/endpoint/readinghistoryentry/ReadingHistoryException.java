package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class ReadingHistoryException extends WebApiDataException {
    public static class UnauthorizedAccessToSessionException extends ReadingHistoryException {
    }

    public static class SessionNotFoundException extends ReadingHistoryException {
    }

    public static class InvalidCredentialException extends UnauthorizedAccessToSessionException {
    }

}
