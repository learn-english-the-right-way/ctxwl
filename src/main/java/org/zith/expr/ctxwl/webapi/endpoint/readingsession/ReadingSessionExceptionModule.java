package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

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

public class ReadingSessionExceptionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReadingSessionExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionModule.ExplainerRepository baseExplainerRepository;
        private final ReadingSessionExceptionExplainerMaker readingSessionExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<ReadingSessionException>> readingSessionExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingSessionException.UnauthorizedAccessToSessionException>>
                unauthorizedAccessToSessionExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingSessionException.SessionNotFoundException>>
                sessionNotFoundExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingSessionException.InvalidCredentialException>>
                invalidCredentialExceptionSupplier;

        @Inject
        public ExplainerRepository(
                WebApiExceptionModule.ExplainerRepository baseExplainerRepository,
                ReadingSessionExceptionExplainerMaker readingSessionExceptionExplainerMaker
        ) {
            this.baseExplainerRepository = baseExplainerRepository;
            this.readingSessionExceptionExplainerMaker = readingSessionExceptionExplainerMaker;

            readingSessionExceptionSupplier = Suppliers.memoize(() ->
                    this.readingSessionExceptionExplainerMaker.make(
                            ReadingSessionErrorCode.INVALID_REQUEST,
                            ReadingSessionException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "You request is invalid.")));
            unauthorizedAccessToSessionExceptionSupplier = Suppliers.memoize(() ->
                    this.readingSessionExceptionExplainerMaker.make(
                            ReadingSessionErrorCode.SESSION_ACCESS_NOT_AUTHORIZED,
                            ReadingSessionException.UnauthorizedAccessToSessionException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You are not authorized to access the reading session you specified. " +
                                            "Sessions are authorized in group basis. " +
                                            "Please check that you are authorized to access the session group.")));
            sessionNotFoundExceptionSupplier = Suppliers.memoize(() ->
                    this.readingSessionExceptionExplainerMaker.make(
                            ReadingSessionErrorCode.SESSION_NOT_FOUND,
                            ReadingSessionException.SessionNotFoundException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You are not authorized to access the reading session you specified. " +
                                            "The session you were trying to access doesn't not exist. " +
                                            "Please check that you've created it properly.")));
            invalidCredentialExceptionSupplier = Suppliers.memoize(() ->
                    this.readingSessionExceptionExplainerMaker.make(
                            ReadingSessionErrorCode.INVALID_CREDENTIAL,
                            ReadingSessionException.InvalidCredentialException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You didn't provide a valid credential, which is required to access a " +
                                            "reading session.")));
        }

        public ExceptionExplainer<ReadingSessionException> readingSessionException() {
            return readingSessionExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingSessionException.UnauthorizedAccessToSessionException> unauthorizedAccessToSessionException() {
            return unauthorizedAccessToSessionExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingSessionException.SessionNotFoundException> sessionNotFoundException() {
            return sessionNotFoundExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingSessionException.InvalidCredentialException> invalidCredentialException() {
            return invalidCredentialExceptionSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(
                    ExceptionExplainerDescriptor.of(
                            readingSessionException(),
                            baseExplainerRepository.webApiExceptionExplainer()
                    ),
                    ExceptionExplainerDescriptor.of(
                            unauthorizedAccessToSessionException(),
                            readingSessionException()
                    ),
                    ExceptionExplainerDescriptor.of(
                            sessionNotFoundException(),
                            readingSessionException()
                    ),
                    ExceptionExplainerDescriptor.of(
                            invalidCredentialException(),
                            readingSessionException()
                    )
            );
        }
    }
}
