package org.zith.expr.ctxwl.webapi.mapper.exception;

import java.util.Optional;

public interface ExceptionExplainer<E extends Exception> {
    Class<E> exceptionClass();

    Optional<ExceptionCauseExplanation> explainIfPossible(E exception);

    static <B extends Exception, E extends Exception> Optional<ExceptionCauseExplanation> explain(ExceptionExplainer<B> explainer, E exception) {
        var exceptionClass = explainer.exceptionClass();
        if (!exceptionClass.isInstance(exception)) return Optional.empty();
        return explainer.explainIfPossible(exceptionClass.cast(exception));
    }
}
