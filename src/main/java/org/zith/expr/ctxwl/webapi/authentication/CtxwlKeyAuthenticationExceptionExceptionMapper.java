package org.zith.expr.ctxwl.webapi.authentication;

import org.zith.expr.ctxwl.webapi.mapper.exception.AbstractExceptionMapper;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.List;

public class CtxwlKeyAuthenticationExceptionExceptionMapper extends AbstractExceptionMapper<CtxwlKeyAuthenticationException> {
    CtxwlKeyAuthenticationExceptionExceptionMapper(StrictExceptionExplainer<CtxwlKeyAuthenticationException> lastExplainer, List<ExceptionExplainer<?>> explainers) {
        super(lastExplainer, explainers);
    }
}
