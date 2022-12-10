package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record EmailRegistrationWebDocument(
        String email,
        Optional<String> password,
        Optional<String> applicationKey,
        Optional<String> confirmationCode,
        Optional<String> userAuthenticationApplicationKey
) {

}
