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
                            fieldNotModifiableException(),
                            readingHistoryException()
                    )
            );
        }
    }
}
