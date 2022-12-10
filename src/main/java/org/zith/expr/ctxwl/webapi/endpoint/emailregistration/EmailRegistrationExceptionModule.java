package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Named;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;

public class EmailRegistrationExceptionModule extends AbstractModule {
    public static final String EMAIL_REGISTRATION_EXCEPTION_EXPLAINER_DESCRIPTOR =
            "email_registration_exception_explainer_descriptor";

    @Provides
    @Singleton
    protected EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker() {
        return new EmailRegistrationExceptionExplainerMaker();
    }

    @Provides
    @Named(EMAIL_REGISTRATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
    @Singleton
    protected ExceptionExplainerDescriptor emailRegistrationExceptionExplainerDescriptor(
            EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker
    ) {
        return ExceptionExplainerDescriptor.of(
                emailRegistrationExceptionExplainerMaker.make(
                        EmailRegistrationErrorCode.INVALID_REQUEST,
                        EmailRegistrationException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                "You request is invalid.")));
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor emailRegistrationExceptionExplainerDescriptorInSet(
            @Named(EMAIL_REGISTRATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor emailRegistrationExceptionExplainerDescriptor
    ) {
        return emailRegistrationExceptionExplainerDescriptor;
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor unauthorizedEmailAddressExceptionExplainerDescriptor(
            EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker,
            @Named(EMAIL_REGISTRATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor emailRegistrationExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                emailRegistrationExceptionExplainerMaker.make(
                        EmailRegistrationErrorCode.UNAUTHORIZED_EMAIL_ADDRESS,
                        EmailRegistrationException.UnauthorizedEmailAddressException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "You are not authorized to confirm the given email address.")),
                emailRegistrationExceptionExplainerDescriptor.explainer());
    }

    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor invalidConfirmationCodeExceptionExplainerDescriptor(
            EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker,
            @Named(EMAIL_REGISTRATION_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor emailRegistrationExceptionExplainerDescriptor
    ) {
        return ExceptionExplainerDescriptor.of(
                emailRegistrationExceptionExplainerMaker.make(
                        EmailRegistrationErrorCode.INVALID_CONFIRMATION_CODE,
                        EmailRegistrationException.InvalidConfirmationCodeException.class,
                        (code, exception) -> SimpleExceptionCauseExplanation.create(
                                code,
                                "The confirmation code you provided is invalid.")),
                emailRegistrationExceptionExplainerDescriptor.explainer());
    }
}
