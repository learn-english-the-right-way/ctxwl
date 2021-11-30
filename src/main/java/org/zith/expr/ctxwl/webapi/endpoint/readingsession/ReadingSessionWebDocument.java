package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ReadingSessionWebDocument(
        String session,
        Long updateTime,
        Long completionTime,
        Long terminationTime
) {
}
