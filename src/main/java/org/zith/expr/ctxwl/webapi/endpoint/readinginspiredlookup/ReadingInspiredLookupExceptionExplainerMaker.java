package org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionExplainerMaker;

import java.util.Map;

public class ReadingInspiredLookupExceptionExplainerMaker
        extends AbstractExceptionExplainerMaker<ReadingInspiredLookupErrorCode, ReadingInspiredLookupException> {
    private static final Map<ReadingInspiredLookupErrorCode, Response.StatusType>
            statusMap = Map.of(
            ReadingInspiredLookupErrorCode.FIELD_NOT_ACCEPTED, Response.Status.CONFLICT
    );

    @Override
    protected Response.StatusType status(ReadingInspiredLookupErrorCode code) {
        return statusMap.getOrDefault(code, Response.Status.BAD_REQUEST);
    }
}
