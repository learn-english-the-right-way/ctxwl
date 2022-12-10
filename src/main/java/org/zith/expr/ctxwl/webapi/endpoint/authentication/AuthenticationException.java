package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import org.zith.expr.ctxwl.webapi.common.WebApiDataException;

public class AuthenticationException extends WebApiDataException {
    public static class InvalidCredentialException extends AuthenticationException {
    }

    public static class UnsupportedAuthenticationMethodException extends AuthenticationException {

        private final String authenticationMethod;

        public UnsupportedAuthenticationMethodException(String authenticationMethod) {
            this.authenticationMethod = authenticationMethod;
        }

        public String getAuthenticationMethod() {
            return authenticationMethod;
        }
    }
}
