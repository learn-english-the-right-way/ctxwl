package org.zith.expr.ctxwl.webapi.authentication;

import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.webapi.common.WebApiErrorCode;
import org.zith.expr.ctxwl.webapi.base.WebApiErrorCodeAdapter;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.List;

public final class CtxwlKeyAuthenticationExceptionMapper extends SimpleExceptionMapper<CtxwlKeyAuthenticationException> {
    public CtxwlKeyAuthenticationExceptionMapper() {
        super(new CtxwlKeyAuthenticationExceptionExplainer(), List.of(), List.of());
    }

    private static class CtxwlKeyAuthenticationExceptionExplainer
            implements Explainer<CtxwlKeyAuthenticationException> {
        private final WebApiErrorCodeAdapter code = new WebApiErrorCodeAdapter(WebApiErrorCode.UNAUTHENTICATED);

        @Override
        public Class<CtxwlKeyAuthenticationException> exceptionClass() {
            return CtxwlKeyAuthenticationException.class;
        }

        @Override
        public Cause explain(CtxwlKeyAuthenticationException exception) {
            return SimpleCause.create(code, "The caller cannot be authenticated. Please check X-Ctxwl-Key header.");
        }
    }
}
