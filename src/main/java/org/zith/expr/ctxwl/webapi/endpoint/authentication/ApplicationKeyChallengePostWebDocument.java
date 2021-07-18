package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import java.util.Optional;

public record ApplicationKeyChallengePostWebDocument(String password, ClientInformation client) {
    public static record ClientInformation(
            Optional<String> os,
            Optional<String> app,
            Optional<String> hardware,
            Optional<String> region,
            Optional<String> language
            ) {
    }
}
