package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Named;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;

public class AuthenticationExceptionModule extends AbstractModule {
    public static final String AUTHENTICATION_EXCEPTION_EXPLAINER_DESCRIPTOR =
            "authenticationExceptionExplainerDescriptor";

    @Provides
    @Singleton
    protected AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker() {
        return new AuthenticationExceptionExplainerMaker();
    }

    @Provides
    @Named(AUTHENTICATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
    @Singleton
    protected ExceptionExplainerDescriptor authenticationExceptionExplainerDescriptor(
            AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker
    ) {
        return ExceptionExplainerDescriptor.of(
                authenticationExceptionExplainerMaker.make(
                        AuthenticationErrorCode.INVALID_REQUEST,
                        AuthenticationException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(code, "You request is invalid.")
                ));
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor authenticationExceptionExplainerDescriptorInSet(
            @Named(AUTHENTICATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor authenticationExceptionExplainerDescriptor) {
        return authenticationExceptionExplainerDescriptor;
    }


    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor invalidCredentialExceptionExplainerDescriptor(
            AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker,
            @Named(AUTHENTICATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor authenticationExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                authenticationExceptionExplainerMaker.make(
                        AuthenticationErrorCode.INVALID_CREDENTIAL,
                        AuthenticationException.InvalidCredentialException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                "The provided credential is invalid.")),
                authenticationExceptionExplainerDescriptor.explainer());
    }


    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor unsupportedAuthenticationMethodExceptionExplainerDescriptor(
            AuthenticationExceptionExplainerMaker authenticationExceptionExplainerMaker,
            @Named(AUTHENTICATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor authenticationExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                authenticationExceptionExplainerMaker.make(
                        AuthenticationErrorCode.UNSUPPORTED_AUTHENTICATION_METHOD,
                        AuthenticationException.UnsupportedAuthenticationMethodException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "The authentication method '%s' is not supported.",
                                exception.getAuthenticationMethod())),
                authenticationExceptionExplainerDescriptor.explainer());
    }
}
