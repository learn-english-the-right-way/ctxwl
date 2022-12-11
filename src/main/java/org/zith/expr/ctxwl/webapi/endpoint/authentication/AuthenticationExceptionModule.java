package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionExplainerRepository;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AuthenticationExceptionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthenticationExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionModule.ExplainerRepository baseExplainerRepository;
        private final AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<AuthenticationException>> authenticationExceptionExplainerSupplier;
        private final Supplier<ExceptionExplainer<AuthenticationException.InvalidCredentialException>>
                invalidCredentialExceptionExplainerSupplier;
        private final Supplier<ExceptionExplainer<AuthenticationException.UnsupportedAuthenticationMethodException>>
                unsupportedAuthenticationMethodExceptionExplainerSupplier;

        @Inject
        public ExplainerRepository(
                WebApiExceptionModule.ExplainerRepository baseExplainerRepository,
                AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker
        ) {
            this.baseExplainerRepository = baseExplainerRepository;
            this.authenticationExceptionExplainerMaker = authenticationExceptionExplainerMaker;

            authenticationExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.authenticationExceptionExplainerMaker.make(
                            AuthenticationErrorCode.INVALID_REQUEST,
                            AuthenticationException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "You request is invalid.")));

            invalidCredentialExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.authenticationExceptionExplainerMaker.make(
                            AuthenticationErrorCode.INVALID_CREDENTIAL,
                            AuthenticationException.InvalidCredentialException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "The provided credential is invalid.")));

            unsupportedAuthenticationMethodExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.authenticationExceptionExplainerMaker.make(
                            AuthenticationErrorCode.UNSUPPORTED_AUTHENTICATION_METHOD,
                            AuthenticationException.UnsupportedAuthenticationMethodException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "The authentication method '%s' is not supported.",
                                    exception.getAuthenticationMethod())));
        }

        public ExceptionExplainer<AuthenticationException> authenticationExceptionExplainer() {
            return authenticationExceptionExplainerSupplier.get();
        }

        public ExceptionExplainer<AuthenticationException.InvalidCredentialException>
        invalidCredentialExceptionExplainer() {
            return invalidCredentialExceptionExplainerSupplier.get();
        }

        public ExceptionExplainer<AuthenticationException.UnsupportedAuthenticationMethodException>
        unsupportedAuthenticationMethodExceptionExplainer() {
            return unsupportedAuthenticationMethodExceptionExplainerSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(
                    ExceptionExplainerDescriptor.of(
                            authenticationExceptionExplainer(),
                            baseExplainerRepository.webApiExceptionExplainer()),
                    ExceptionExplainerDescriptor.of(
                            invalidCredentialExceptionExplainer(),
                            authenticationExceptionExplainer()),
                    ExceptionExplainerDescriptor.of(
                            unsupportedAuthenticationMethodExceptionExplainer(),
                            authenticationExceptionExplainer()));
        }
    }
}
