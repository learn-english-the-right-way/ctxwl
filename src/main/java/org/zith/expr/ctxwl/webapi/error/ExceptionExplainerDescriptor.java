package org.zith.expr.ctxwl.webapi.error;

import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;

import java.util.Optional;

public record ExceptionExplainerDescriptor(
        ExceptionExplainer<?> explainer,
        Optional<ExceptionExplainer<?>> generalization
) {
    public static ExceptionExplainerDescriptor of(ExceptionExplainer<?> explainer) {
        return new ExceptionExplainerDescriptor(explainer, Optional.empty());
    }

    public static ExceptionExplainerDescriptor of(
            ExceptionExplainer<?> explainer, ExceptionExplainer<?> generalization) {
        return new ExceptionExplainerDescriptor(explainer, Optional.of(generalization));
    }
}
