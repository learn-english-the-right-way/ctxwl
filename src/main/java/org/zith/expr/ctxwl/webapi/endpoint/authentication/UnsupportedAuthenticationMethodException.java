package org.zith.expr.ctxwl.webapi.endpoint.authentication;

public class UnsupportedAuthenticationMethodException extends AuthenticationException {

    private final String authenticationMethod;

    public UnsupportedAuthenticationMethodException(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }
}
