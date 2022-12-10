package org.zith.expr.ctxwl.webapi.mapper.exception;

import java.util.Optional;

public interface StrictExceptionExplainer<E extends Exception> extends ExceptionExplainer<E> {
    default Optional<ExceptionCauseExplanation> explainIfPossible(E exception) {
        return Optional.of(explain(exception));
    }

    ExceptionCauseExplanation explain(E exception);
}
