package org.zith.expr.ctxwl.webapi.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Named;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionMapperMaker;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.LinkedList;
import java.util.Set;

public class WebApiExceptionModule extends AbstractModule {
    public static final String WEB_API_EXCEPTION_EXPLAINER_DESCRIPTOR = "web_api_exception_explainer_descriptor";
    public static final String WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS = "web_api_exception_explainers_descriptors";

    @Provides
    @Singleton
    protected WebApiExceptionExplainerMaker webApiExceptionExplainerMaker() {
        return new WebApiExceptionExplainerMaker();
    }

    @Provides
    @Named(WEB_API_EXCEPTION_EXPLAINER_DESCRIPTOR)
    @Singleton
    protected ExceptionExplainerDescriptor webApiExceptionExplainerDescriptor(
            WebApiExceptionExplainerMaker webApiExceptionExplainerMaker
    ) {
        return ExceptionExplainerDescriptor.of(
                webApiExceptionExplainerMaker.make(
                        WebApiErrorCode.DATA_ERROR,
                        WebApiDataException.class,
                        ((code, exception) -> SimpleExceptionCauseExplanation
                                .create(code, "This endpoint doesn't accept your request.")))
        );
    }

    @ProvidesIntoSet
    @Named(WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor webApiExceptionExplainerDescriptorInSet(
            @Named(WEB_API_EXCEPTION_EXPLAINER_DESCRIPTOR)
            ExceptionExplainerDescriptor webApiExceptionExplainerDescriptor) {
        return webApiExceptionExplainerDescriptor;
    }

    @Provides
    @Singleton
    protected ExceptionMapper<WebApiDataException> webApiExceptionMapper(
            @Named(WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
            Set<ExceptionExplainerDescriptor> explainerDescriptors
    ) {
        var maker = new AbstractExceptionMapperMaker<WebApiDataException, WebApiDataExceptionMapper>() {
            @Override
            protected WebApiDataExceptionMapper newExceptionMapper(
                    StrictExceptionExplainer<WebApiDataException> lastExplainer,
                    LinkedList<ExceptionExplainer<?>> chain
            ) {
                return new WebApiDataExceptionMapper(lastExplainer, chain);
            }
        };
        return maker.create(WebApiDataException.class, explainerDescriptors.stream().toList());
    }
}
