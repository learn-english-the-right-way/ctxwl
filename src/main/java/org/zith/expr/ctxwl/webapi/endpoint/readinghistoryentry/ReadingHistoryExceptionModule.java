package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

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

public class ReadingHistoryExceptionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReadingHistoryExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionModule.ExplainerRepository baseExplainerRepository;
        private final ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<ReadingHistoryException>> readingHistoryExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingHistoryException.UnauthorizedAccessToSessionException>>
                unauthorizedAccessToSessionExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingHistoryException.SessionNotFoundException>>
                sessionNotFoundExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingHistoryException.InvalidCredentialException>>
                invalidCredentialExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingHistoryException.FieldNotAcceptedException>>
                fieldNotModifiableExceptionSupplier;

        @Inject
        public ExplainerRepository(
                WebApiExceptionModule.ExplainerRepository baseExplainerRepository,
                ReadingHistoryExceptionExplainerMaker readingHistoryExceptionExplainerMaker
        ) {
            this.baseExplainerRepository = baseExplainerRepository;
            this.readingHistoryExceptionExplainerMaker = readingHistoryExceptionExplainerMaker;

            readingHistoryExceptionSupplier = Suppliers.memoize(() ->
                    this.readingHistoryExceptionExplainerMaker.make(
                            ReadingHistoryErrorCode.INVALID_REQUEST,
                            ReadingHistoryException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "You request is invalid.")));
            unauthorizedAccessToSessionExceptionSupplier = Suppliers.memoize(() ->
                    this.readingHistoryExceptionExplainerMaker.make(
                            ReadingHistoryErrorCode.SESSION_ACCESS_NOT_AUTHORIZED,
                            ReadingHistoryException.UnauthorizedAccessToSessionException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You are not authorized to access the reading session you specified. " +
                                            "Sessions are authorized in group basis. " +
                                            "Please check that you are authorized to access the session group.")));
            sessionNotFoundExceptionSupplier = Suppliers.memoize(() ->
                    this.readingHistoryExceptionExplainerMaker.make(
                            ReadingHistoryErrorCode.SESSION_NOT_FOUND,
                            ReadingHistoryException.SessionNotFoundException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You are not authorized to access the reading session you specified. " +
                                            "The session you were trying to access doesn't not exist. " +
                                            "Please check that you've created it properly.")));
            invalidCredentialExceptionSupplier = Suppliers.memoize(() ->
                    this.readingHistoryExceptionExplainerMaker.make(
                            ReadingHistoryErrorCode.INVALID_CREDENTIAL,
                            ReadingHistoryException.InvalidCredentialException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You didn't provide a valid credential, which is required to access a " +
                                            "reading session.")));
            fieldNotModifiableExceptionSupplier = Suppliers.memoize(() ->
                    this.readingHistoryExceptionExplainerMaker.make(
                            ReadingHistoryErrorCode.FIELD_NOT_ACCEPTED,
                            ReadingHistoryException.FieldNotAcceptedException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You were trying to update field '%s' to an unacceptable value."
                                            .formatted(exception.getFieldName()))));
        }

        public ExceptionExplainer<ReadingHistoryException> readingHistoryException() {
            return readingHistoryExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingHistoryException.UnauthorizedAccessToSessionException> unauthorizedAccessToSessionException() {
            return unauthorizedAccessToSessionExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingHistoryException.SessionNotFoundException> sessionNotFoundException() {
            return sessionNotFoundExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingHistoryException.InvalidCredentialException> invalidCredentialException() {
            return invalidCredentialExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingHistoryException.FieldNotAcceptedException> fieldNotModifiableException() {
            return fieldNotModifiableExceptionSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(
                    ExceptionExplainerDescriptor.of(
                            readingHistoryException(),
                            baseExplainerRepository.webApiExceptionExplainer()
                    ),
                    ExceptionExplainerDescriptor.of(
                            unauthorizedAccessToSessionException(),
                            readingHistoryException()
                    ),
                    ExceptionExplainerDescriptor.of(
                            sessionNotFoundException(),
                            readingHistoryException()
                    ),
                    ExceptionExplainerDescriptor.of(
                            invalidCredentialException(),
                            readingHistoryException()
                    ),
                    ExceptionExplainerDescriptor.of(
                            fieldNotModifiableException(),
                            readingHistoryException()
                    )
            );
        }
    }
}
