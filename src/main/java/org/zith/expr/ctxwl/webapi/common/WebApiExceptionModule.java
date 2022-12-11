package org.zith.expr.ctxwl.webapi.common;

import com.google.common.base.Suppliers;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionMapperMaker;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class WebApiExceptionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(typeOfExeptionMapperOfWebApiDataException()).toProvider(ExceptionMapperProvider.class)
                .in(Scopes.SINGLETON);
        bind(WebApiExceptionExplainerMaker.class).in(Scopes.SINGLETON);
        bind(ExplainerRepository.class).in(Scopes.SINGLETON);
        Multibinder.newSetBinder(binder(), WebApiExceptionExplainerRepository.class)
                .addBinding().to(ExplainerRepository.class);
    }

    private static TypeLiteral<ExceptionMapper<WebApiDataException>> typeOfExeptionMapperOfWebApiDataException() {
        return new TypeLiteral<>() {
        };
    }

    public static class ExceptionMapperProvider implements Provider<ExceptionMapper<WebApiDataException>> {
        private final Set<WebApiExceptionExplainerRepository> explainerRepositories;

        @Inject
        public ExceptionMapperProvider(Set<WebApiExceptionExplainerRepository> explainerRepositories) {
            this.explainerRepositories = explainerRepositories;
        }

        @Override
        public ExceptionMapper<WebApiDataException> get() {
            var maker = new AbstractExceptionMapperMaker<WebApiDataException, WebApiDataExceptionMapper>() {
                @Override
                protected WebApiDataExceptionMapper newExceptionMapper(
                        StrictExceptionExplainer<WebApiDataException> lastExplainer,
                        LinkedList<ExceptionExplainer<?>> chain
                ) {
                    return new WebApiDataExceptionMapper(lastExplainer, chain);
                }
            };
            return maker.create(
                    WebApiDataException.class,
                    explainerRepositories.stream().flatMap(r -> r.descriptors().stream()).toList());
        }
    }

    public static class ExplainerRepository implements WebApiExceptionExplainerRepository {
        private final WebApiExceptionExplainerMaker webApiExceptionExplainerMaker;
        private final Supplier<ExceptionExplainer<WebApiDataException>> webApiExceptionExplainerSupplier;

        @Inject
        public ExplainerRepository(WebApiExceptionExplainerMaker webApiExceptionExplainerMaker) {
            this.webApiExceptionExplainerMaker = webApiExceptionExplainerMaker;
            webApiExceptionExplainerSupplier = Suppliers.memoize(() ->
                    this.webApiExceptionExplainerMaker.make(
                            WebApiErrorCode.DATA_ERROR,
                            WebApiDataException.class,
                            ((code, exception) -> SimpleExceptionCauseExplanation
                                    .create(code, "This endpoint doesn't accept your request."))));
        }

        public ExceptionExplainer<WebApiDataException> webApiExceptionExplainer() {
            return webApiExceptionExplainerSupplier.get();
        }

        @Override
        public Collection<ExceptionExplainerDescriptor> descriptors() {
            return List.of(ExceptionExplainerDescriptor.of(webApiExceptionExplainer()));
        }
    }
}
