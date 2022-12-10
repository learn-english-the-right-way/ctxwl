package org.zith.expr.ctxwl.webapi.mapper.exception;

import org.jetbrains.annotations.Nullable;

public interface ExceptionCauseExplanation {
    AbstractExceptionMapper.Code code();

    String message();

    @Nullable
    Object payload();
}
