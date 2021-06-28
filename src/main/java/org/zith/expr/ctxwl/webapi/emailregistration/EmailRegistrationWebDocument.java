package org.zith.expr.ctxwl.webapi.emailregistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.zith.expr.ctxwl.webapi.session.SessionToken;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record EmailRegistrationWebDocument(
        String email,
        Optional<String> password,
        Optional<String> applicationKey,
        Optional<String> confirmationCode,
        Optional<Integer> userId,
        Optional<String> userAuthenticationApplicationKey,
        Optional<SessionToken> userSessionToken) {

}
