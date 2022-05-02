package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.inject.Inject;
import org.zith.expr.ctxwl.webapi.common.WebApiDataExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.List;
import java.util.Objects;

public class EmailRegistrationExceptionMapper extends SimpleExceptionMapper<EmailRegistrationException> {
    @Inject
    public EmailRegistrationExceptionMapper(WebApiDataExceptionExplainer webApiDataExceptionExplainer) {
        super(
                EmailRegistrationExceptionExplainer.create(
                        EmailRegistrationErrorCode.INVALID_REQUEST,
                        EmailRegistrationException.class,
                        (code, exception) -> SimpleCause.create(code, "You request is invalid.")
                ),
                List.of(
                        EmailRegistrationExceptionExplainer.create(
                                EmailRegistrationErrorCode.UNAUTHORIZED_EMAIL_ADDRESS,
                                EmailRegistrationException.UnauthorizedEmailAddressException.class,
                                (code, exception) -> SimpleCause.create(
                                        code,
                                        "You are not authorized to confirm the given email address.")
                        ),
                        EmailRegistrationExceptionExplainer.create(
                                EmailRegistrationErrorCode.INVALID_CONFIRMATION_CODE,
                                EmailRegistrationException.InvalidConfirmationCodeException.class,
                                (code, exception) -> SimpleCause.create(
                                        code,
                                        "The confirmation code you provided is invalid.")
                        )
                ),
                List.of(webApiDataExceptionExplainer));
    }

    private static class EmailRegistrationExceptionExplainer<E extends EmailRegistrationException> implements Explainer<E> {

        private final EmailRegistrationErrorCodeAdapter code;
        private final Class<E> exceptionClass;
        private final CauseMaker<E> causeMaker;

        private EmailRegistrationExceptionExplainer(
                EmailRegistrationErrorCode code,
                Class<E> exceptionClass,
                CauseMaker<E> causeMaker
        ) {
            Objects.requireNonNull(code);
            Objects.requireNonNull(exceptionClass);
            this.code = new EmailRegistrationErrorCodeAdapter(code);
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

        public static <E extends EmailRegistrationException> EmailRegistrationExceptionExplainer<E>
        create(EmailRegistrationErrorCode code, Class<E> exceptionClass, CauseMaker<E> causeMaker) {
            return new EmailRegistrationExceptionExplainer<E>(code, exceptionClass, causeMaker);
        }

        public interface CauseMaker<E extends EmailRegistrationException> {
            Cause makeCause(Code code, E exception);
        }
    }

}
