package org.zith.expr.ctxwl.webapi.session;

import java.time.Instant;

public record SessionToken(
        String token,
        Instant expiry) {
}
