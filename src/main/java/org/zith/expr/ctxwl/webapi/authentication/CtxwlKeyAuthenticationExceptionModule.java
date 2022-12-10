package org.zith.expr.ctxwl.webapi.authentication;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.google.inject.name.Named;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.zith.expr.ctxwl.webapi.common.WebApiErrorCode;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionExplainerMaker;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.error.AbstractExceptionMapperMaker;
import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.SimpleExceptionCauseExplanation;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.LinkedList;
import java.util.Set;

public class CtxwlKeyAuthenticationExceptionModule extends AbstractModule {
    @ProvidesIntoSet
    @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
    protected ExceptionExplainerDescriptor ctxwlKeyAuthenticationExceptionExplainerDescriptor(
            WebApiExceptionExplainerMaker webApiExceptionExplainerMaker
    ) {
        return ExceptionExplainerDescriptor.of(
                webApiExceptionExplainerMaker.make(
                        WebApiErrorCode.UNAUTHENTICATED,
                        CtxwlKeyAuthenticationException.class,
                        ((code, exception) -> SimpleExceptionCauseExplanation.create(code,
                                "The caller cannot be authenticated. Please check X-Ctxwl-Key header."))));
    }

    @Provides
    @Singleton
    protected ExceptionMapper<CtxwlKeyAuthenticationException> webApiExceptionMapper(
            @Named(WebApiExceptionModule.WEB_API_EXCEPTION_EXPLAINER_DESCRIPTORS)
            Set<ExceptionExplainerDescriptor> explainerDescriptors
    ) {
        var maker = new AbstractExceptionMapperMaker
                <CtxwlKeyAuthenticationException, CtxwlKeyAuthenticationExceptionExceptionMapper>() {
            @Override
            protected CtxwlKeyAuthenticationExceptionExceptionMapper newExceptionMapper(
                    StrictExceptionExplainer<CtxwlKeyAuthenticationException> lastExplainer,
                    LinkedList<ExceptionExplainer<?>> chain) {
                return new CtxwlKeyAuthenticationExceptionExceptionMapper(lastExplainer, chain);
            }
        };
        return maker.create(CtxwlKeyAuthenticationException.class, explainerDescriptors.stream().toList());
    }
}
