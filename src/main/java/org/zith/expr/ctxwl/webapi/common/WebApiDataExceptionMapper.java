package org.zith.expr.ctxwl.webapi.common;

import org.zith.expr.ctxwl.webapi.mapper.exception.AbstractExceptionMapper;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.List;

public class WebApiDataExceptionMapper extends AbstractExceptionMapper<WebApiDataException> {
    WebApiDataExceptionMapper(StrictExceptionExplainer<WebApiDataException> lastExplainer, List<ExceptionExplainer<?>> explainers) {
        super(lastExplainer, explainers);
    }
}
