package org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup;

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

public class ReadingInspiredLookupExceptionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReadingInspiredLookupExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionModule.ExplainerRepository baseExplainerRepository;
        private final ReadingInspiredLookupExceptionExplainerMaker readingInspiredLookupExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<ReadingInspiredLookupException>> readingInspiredLookupExceptionSupplier;
        private final Supplier<ExceptionExplainer<ReadingInspiredLookupException.FieldNotAcceptedException>>
                fieldNotModifiableExceptionSupplier;

        @Inject
        public ExplainerRepository(
                WebApiExceptionModule.ExplainerRepository baseExplainerRepository,
                ReadingInspiredLookupExceptionExplainerMaker readingInspiredLookupExceptionExplainerMaker
        ) {
            this.baseExplainerRepository = baseExplainerRepository;
            this.readingInspiredLookupExceptionExplainerMaker = readingInspiredLookupExceptionExplainerMaker;

            readingInspiredLookupExceptionSupplier = Suppliers.memoize(() ->
                    this.readingInspiredLookupExceptionExplainerMaker.make(
                            ReadingInspiredLookupErrorCode.INVALID_REQUEST,
                            ReadingInspiredLookupException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                    "You request is invalid.")));
            fieldNotModifiableExceptionSupplier = Suppliers.memoize(() ->
                    this.readingInspiredLookupExceptionExplainerMaker.make(
                            ReadingInspiredLookupErrorCode.FIELD_NOT_ACCEPTED,
                            ReadingInspiredLookupException.FieldNotAcceptedException.class,
                            (code, exception) -> SimpleExceptionCauseExplanation.create(
                                    code,
                                    "You were trying to update field '%s' to an unacceptable value."
                                            .formatted(exception.getFieldName()))));
        }

        public ExceptionExplainer<ReadingInspiredLookupException> readingInspiredLookupException() {
            return readingInspiredLookupExceptionSupplier.get();
        }

        public ExceptionExplainer<ReadingInspiredLookupException.FieldNotAcceptedException> fieldNotModifiableException() {
            return fieldNotModifiableExceptionSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(
                    ExceptionExplainerDescriptor.of(
                            readingInspiredLookupException(),
                            baseExplainerRepository.webApiExceptionExplainer()
                    ),
                    ExceptionExplainerDescriptor.of(
                            fieldNotModifiableException(),
                            readingInspiredLookupException()
                    )
            );
        }
    }
}
