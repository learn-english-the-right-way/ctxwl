package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

import java.util.Map;

public class ReadingSessionExceptionExplainerMaker
        extends AbstractExceptionExplainerMaker<ReadingSessionErrorCode, ReadingSessionException> {
    private static final Map<ReadingSessionErrorCode, Response.StatusType>
            statusMap = Map.of(
            ReadingSessionErrorCode.SESSION_ACCESS_NOT_AUTHORIZED, Response.Status.FORBIDDEN,
            ReadingSessionErrorCode.SESSION_NOT_FOUND, Response.Status.NOT_FOUND,
            ReadingSessionErrorCode.INVALID_CREDENTIAL, Response.Status.FORBIDDEN
    );

    @Override
    protected Response.StatusType status(ReadingSessionErrorCode code) {
        return statusMap.getOrDefault(code, Response.Status.BAD_REQUEST);
    }
}
