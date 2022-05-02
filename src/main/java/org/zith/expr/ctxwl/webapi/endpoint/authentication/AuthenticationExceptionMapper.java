package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.webapi.common.WebApiDataExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.List;

public class AuthenticationExceptionMapper extends SimpleExceptionMapper<AuthenticationException> {
    @Inject
    public AuthenticationExceptionMapper(WebApiDataExceptionExplainer webApiDataExceptionExplainer) {
        super(
                new AuthenticationExceptionExplainer(),
                List.of(
                        new InvalidCredentialExceptionExplainer(),
                        new UnsupportedAuthenticationMethodExceptionExplainer()
                ),
                List.of(webApiDataExceptionExplainer)
        );
    }

    private static class AuthenticationExceptionExplainer implements Explainer<AuthenticationException> {

        private final AuthenticationErrorCodeAdapter code =
                new AuthenticationErrorCodeAdapter(AuthenticationErrorCode.INVALID_REQUEST);

        @Override
        public Class<AuthenticationException> exceptionClass() {
            return AuthenticationException.class;
        }

        @Override
        public Cause explain(AuthenticationException exception) {
            return SimpleCause.create(code, "You request is invalid.");
        }
    }

    private static class InvalidCredentialExceptionExplainer implements Explainer<InvalidCredentialException> {

        private final AuthenticationErrorCodeAdapter code =
                new AuthenticationErrorCodeAdapter(AuthenticationErrorCode.INVALID_CREDENTIAL);

        @Override
        public Class<InvalidCredentialException> exceptionClass() {
            return InvalidCredentialException.class;
        }

        @Override
        public Cause explain(InvalidCredentialException exception) {
            return SimpleCause.create(code, "The provided credential is invalid.");
        }
    }

    private static class UnsupportedAuthenticationMethodExceptionExplainer
            implements Explainer<UnsupportedAuthenticationMethodException> {

        private final AuthenticationErrorCodeAdapter code =
                new AuthenticationErrorCodeAdapter(AuthenticationErrorCode.UNSUPPORTED_AUTHENTICATION_METHOD);

        @Override
        public Class<UnsupportedAuthenticationMethodException> exceptionClass() {
            return UnsupportedAuthenticationMethodException.class;
        }

        @Override
        public Cause explain(UnsupportedAuthenticationMethodException exception) {
            return SimpleCause.create(code, "The authentication method '%s' is not supported.", exception.getAuthenticationMethod());
        }
    }
}
