package org.zith.expr.ctxwl.webapi.common;

import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.webapi.base.WebApiDataException;
import org.zith.expr.ctxwl.webapi.base.WebApiErrorCodeAdapter;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

public class WebApiDataExceptionExplainer implements SimpleExceptionMapper.Explainer<WebApiDataException> {

    private final WebApiErrorCodeAdapter code = new WebApiErrorCodeAdapter(WebApiErrorCode.DATA_ERROR);

    @Override
    public Class<WebApiDataException> exceptionClass() {
        return WebApiDataException.class;
    }

    @Override
    public SimpleExceptionMapper.Cause explain(WebApiDataException exception) {
        return SimpleExceptionMapper.SimpleCause.create(code, "This endpoint doesn't accept your request.");
    }
}
