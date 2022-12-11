package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

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

public class EmailRegistrationExceptionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EmailRegistrationExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionModule.ExplainerRepository baseExplainerRepository;
        private final EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<EmailRegistrationException>>
                emailRegistrationExceptionExplainerSupplier;
        private final Supplier<ExceptionExplainer<EmailRegistrationException.UnauthorizedEmailAddressException>>
                unauthorizedEmailAddressExceptionExplainerSupplier;
        private final Supplier<ExceptionExplainer<EmailRegistrationException.InvalidConfirmationCodeException>>
                invalidConfirmationCodeExceptionExplainerSupplier;

        @Inject
        public ExplainerRepository(
                WebApiExceptionModule.ExplainerRepository baseExplainerRepository,
                EmailRegistrationExceptionExplainerMaker emailRegistrationExceptionExplainerMaker
        ) {
            this.baseExplainerRepository = baseExplainerRepository;
            this.emailRegistrationExceptionExplainerMaker = emailRegistrationExceptionExplainerMaker;

            emailRegistrationExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.emailRegistrationExceptionExplainerMaker.make(
                            EmailRegistrationErrorCode.INVALID_REQUEST,
                            EmailRegistrationException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "You request is invalid.")));
            unauthorizedEmailAddressExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.emailRegistrationExceptionExplainerMaker.make(
                            EmailRegistrationErrorCode.UNAUTHORIZED_EMAIL_ADDRESS,
                            EmailRegistrationException.UnauthorizedEmailAddressException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You are not authorized to confirm the given email address.")));
            invalidConfirmationCodeExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.emailRegistrationExceptionExplainerMaker.make(
                            EmailRegistrationErrorCode.INVALID_CONFIRMATION_CODE,
                            EmailRegistrationException.InvalidConfirmationCodeException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "The confirmation code you provided is invalid.")));
        }

        public ExceptionExplainer<EmailRegistrationException>
        emailRegistrationExceptionExplainer() {
            return emailRegistrationExceptionExplainerSupplier.get();
        }

        public ExceptionExplainer<EmailRegistrationException.UnauthorizedEmailAddressException>
        unauthorizedEmailAddressExceptionExplainer() {
            return unauthorizedEmailAddressExceptionExplainerSupplier.get();
        }

        public ExceptionExplainer<EmailRegistrationException.InvalidConfirmationCodeException>
        invalidConfirmationCodeExceptionExplainer() {
            return invalidConfirmationCodeExceptionExplainerSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(
                    ExceptionExplainerDescriptor.of(
                            emailRegistrationExceptionExplainer(),
                            baseExplainerRepository.webApiExceptionExplainer()),
                    ExceptionExplainerDescriptor.of(
                            unauthorizedEmailAddressExceptionExplainer(),
                            emailRegistrationExceptionExplainer()),
                    ExceptionExplainerDescriptor.of(
                            invalidConfirmationCodeExceptionExplainer(),
                            emailRegistrationExceptionExplainer()));
        }
    }
}
