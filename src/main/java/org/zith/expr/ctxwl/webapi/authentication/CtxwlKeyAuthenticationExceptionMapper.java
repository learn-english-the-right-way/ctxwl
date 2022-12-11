package org.zith.expr.ctxwl.webapi.authentication;

import org.zith.expr.ctxwl.webapi.mapper.exception.AbstractExceptionMapper;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.List;

public class CtxwlKeyAuthenticationExceptionMapper extends AbstractExceptionMapper<CtxwlKeyAuthenticationException> {
    CtxwlKeyAuthenticationExceptionMapper(
            StrictExceptionExplainer<CtxwlKeyAuthenticationException> lastExplainer,
            List<ExceptionExplainer<?>> explainers
    ) {
        super(lastExplainer, explainers);
    }
}
