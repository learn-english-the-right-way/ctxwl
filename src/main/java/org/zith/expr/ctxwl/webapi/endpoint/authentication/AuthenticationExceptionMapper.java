package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.base.SimpleErrorCodeAdapter;
import org.zith.expr.ctxwl.webapi.common.WebApiDataExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.List;
import java.util.Objects;

public class AuthenticationExceptionMapper extends SimpleExceptionMapper<AuthenticationException> {
    @Inject
    public AuthenticationExceptionMapper(WebApiDataExceptionExplainer webApiDataExceptionExplainer) {
        super(
                AuthenticationExceptionExplainer.create(
                        AuthenticationErrorCode.INVALID_REQUEST,
                        AuthenticationException.class,
                        (code, exception) -> SimpleCause.create(code, "You request is invalid.")
                ),
                List.of(
                        AuthenticationExceptionExplainer.create(
                                AuthenticationErrorCode.INVALID_CREDENTIAL,
                                AuthenticationException.InvalidCredentialException.class,
                                (code, exception) -> SimpleCause.create(code, "The provided credential is invalid.")
                        ),
                        AuthenticationExceptionExplainer.create(
                                AuthenticationErrorCode.UNSUPPORTED_AUTHENTICATION_METHOD,
                                AuthenticationException.UnsupportedAuthenticationMethodException.class,
                                (code, exception) -> SimpleCause.create(
                                        code,
                                        "The authentication method '%s' is not supported.",
                                        exception.getAuthenticationMethod())
                        )
                ),
                List.of(webApiDataExceptionExplainer)
        );
    }

    private static class AuthenticationExceptionExplainer<E extends AuthenticationException> implements Explainer<E> {

        private final AuthenticationErrorCodeAdapter code;
        private final Class<E> exceptionClass;
        private final CauseMaker<E> causeMaker;

        private AuthenticationExceptionExplainer(
                AuthenticationErrorCode code,
                Class<E> exceptionClass,
                CauseMaker<E> causeMaker
        ) {
            Objects.requireNonNull(code);
            Objects.requireNonNull(exceptionClass);
            this.code = new AuthenticationErrorCodeAdapter(code);
            this.exceptionClass = exceptionClass;
            this.causeMaker = causeMaker;
        }

        @Override
        public Class<E> exceptionClass() {
            return exceptionClass;
        }

        @Override
        public Cause explain(E exception) {
            return causeMaker.makeCause(code, exception);
        }

        public static <E extends AuthenticationException> AuthenticationExceptionExplainer<E> create(
                AuthenticationErrorCode code,
                Class<E> exceptionClass,
                CauseMaker<E> causeMaker
        ) {
            return new AuthenticationExceptionExplainer<>(code, exceptionClass, causeMaker);
        }

        public interface CauseMaker<E extends AuthenticationException> {
            Cause makeCause(Code code, E exception);
        }
    }

    public static class AuthenticationErrorCodeAdapter extends SimpleErrorCodeAdapter<AuthenticationErrorCode> {

        public AuthenticationErrorCodeAdapter(AuthenticationErrorCode code) {
            super(code);
        }

        @Override
        public Response.StatusType status() {
            switch (code) {
                case INVALID_REQUEST -> {
                    return Response.Status.BAD_REQUEST;
                }
                case INVALID_CREDENTIAL -> {
                    return Response.Status.UNAUTHORIZED;
                }
                case UNSUPPORTED_AUTHENTICATION_METHOD -> {
                    return Response.Status.NOT_FOUND;
                }
            }

            return super.status();
        }
    }
}
