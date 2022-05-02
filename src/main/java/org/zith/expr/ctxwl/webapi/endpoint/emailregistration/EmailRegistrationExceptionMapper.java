package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.inject.Inject;
import org.zith.expr.ctxwl.webapi.common.WebApiDataExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.List;

public class EmailRegistrationExceptionMapper extends SimpleExceptionMapper<EmailRegistrationException> {
    @Inject
    public EmailRegistrationExceptionMapper(WebApiDataExceptionExplainer webApiDataExceptionExplainer) {
        super(
                new EmailRegistrationExceptionExplainer(),
                List.of(
                        new UnauthorizedEmailAddressExceptionExplainer(),
                        new InvalidConfirmationCodeExceptionExplainer()
                ),
                List.of(webApiDataExceptionExplainer));
    }

    private static class EmailRegistrationExceptionExplainer implements Explainer<EmailRegistrationException> {
        private final EmailRegistrationErrorCodeAdapter code =
                new EmailRegistrationErrorCodeAdapter(EmailRegistrationErrorCode.INVALID_REQUEST);

        @Override
        public Class<EmailRegistrationException> exceptionClass() {
            return EmailRegistrationException.class;
        }

        @Override
        public Cause explain(EmailRegistrationException exception) {
            return SimpleCause.create(code, "You request is invalid.");
        }
    }

    private static class UnauthorizedEmailAddressExceptionExplainer implements Explainer<UnauthorizedEmailAddressException> {
        private final EmailRegistrationErrorCodeAdapter code =
                new EmailRegistrationErrorCodeAdapter(EmailRegistrationErrorCode.UNAUTHORIZED_EMAIL_ADDRESS);

        @Override
        public Class<UnauthorizedEmailAddressException> exceptionClass() {
            return UnauthorizedEmailAddressException.class;
        }

        @Override
        public Cause explain(UnauthorizedEmailAddressException exception) {
            return SimpleCause.create(code, "You are not authorized to confirm the given email address.");
        }
    }

    private static class InvalidConfirmationCodeExceptionExplainer implements Explainer<InvalidConfirmationCodeException> {
        private final EmailRegistrationErrorCodeAdapter code =
                new EmailRegistrationErrorCodeAdapter(EmailRegistrationErrorCode.INVALID_CONFIRMATION_CODE);

        @Override
        public Class<InvalidConfirmationCodeException> exceptionClass() {
            return InvalidConfirmationCodeException.class;
        }

        @Override
        public Cause explain(InvalidConfirmationCodeException exception) {
            return SimpleCause.create(code, "The confirmation code you provided is invalid.");
        }
    }
}
