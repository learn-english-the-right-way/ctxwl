package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

import java.util.Map;

public class ReadingHistoryExceptionExplainerMaker
        extends AbstractExceptionExplainerMaker<ReadingHistoryErrorCode, ReadingHistoryException> {
    private static final Map<ReadingHistoryErrorCode, Response.StatusType>
            statusMap = Map.of(
            ReadingHistoryErrorCode.SESSION_ACCESS_NOT_AUTHORIZED, Response.Status.FORBIDDEN,
            ReadingHistoryErrorCode.SESSION_NOT_FOUND, Response.Status.NOT_FOUND,
            ReadingHistoryErrorCode.INVALID_CREDENTIAL, Response.Status.FORBIDDEN,
            ReadingHistoryErrorCode.FIELD_NOT_ACCEPTED, Response.Status.CONFLICT
    );

    @Override
    protected Response.StatusType status(ReadingHistoryErrorCode code) {
        return statusMap.getOrDefault(code, Response.Status.BAD_REQUEST);
    }
}
