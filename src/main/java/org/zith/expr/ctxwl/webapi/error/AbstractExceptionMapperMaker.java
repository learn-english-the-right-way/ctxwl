package org.zith.expr.ctxwl.webapi.error;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.webapi.mapper.exception.AbstractExceptionMapper;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.*;

public abstract class AbstractExceptionMapperMaker<E extends Exception, M extends AbstractExceptionMapper<E>> {
    public M create(Class<E> exceptionClass, List<ExceptionExplainerDescriptor> explainerDeclarations) {
        class ExplainerInformation {
            final ExceptionExplainer<?> explainer;
            boolean declared = false;
            @Nullable ExceptionExplainer<?> generalization = null;
            @SuppressWarnings("FieldMayBeFinal")
            LinkedList<ExceptionExplainer<?>> specialization = new LinkedList<>();

            ExplainerInformation(ExceptionExplainer<?> explainer) {
                this.explainer = explainer;
            }
        }

        var explainers = new IdentityHashMap<ExceptionExplainer<?>, ExplainerInformation>();

        for (var explainerDeclaration : explainerDeclarations) {
            var explainer = explainerDeclaration.explainer();

            var explainerInformation = explainers.computeIfAbsent(explainer, ExplainerInformation::new);
            explainerInformation.declared = true;
            explainerInformation.generalization = explainerDeclaration.generalization().orElse(null);

            explainerDeclaration.generalization().ifPresent(base -> {
                var baseExplainerInformation = explainers.computeIfAbsent(base, ExplainerInformation::new);
                baseExplainerInformation.specialization.add(explainer);
            });
        }

        for (var explainerInformation : explainers.values()) {
            Preconditions.checkArgument(explainerInformation.declared,
                    "Explainer %s is referenced but not declared", explainerInformation.explainer);
        }

        M result = null;
        for (var explainerInformation : explainers.values()) {
            if (explainerInformation.generalization != null) {
                continue;
            }

            if (!explainerInformation.explainer.exceptionClass().isAssignableFrom(exceptionClass)) {
                continue;
            }

            Preconditions.checkArgument(result == null, "Multiple hierarchies for %s are defined", exceptionClass);

            StrictExceptionExplainer<E> lastExplainer;
            Preconditions.checkArgument(
                    explainerInformation.explainer instanceof StrictExceptionExplainer<?>,
                    "The root explainer %s needs to be a strict one", explainerInformation.explainer);

            //noinspection unchecked
            lastExplainer = (StrictExceptionExplainer<E>) explainerInformation.explainer;

            var chain = new LinkedList<ExceptionExplainer<?>>();

            class Frame {
                final ExplainerInformation explainerInformation;
                Iterator<ExceptionExplainer<?>> specialization = null;

                Frame(ExplainerInformation explainerInformation) {
                    this.explainerInformation = explainerInformation;
                }
            }

            var stack = new Stack<Frame>();
            stack.push(new Frame(explainerInformation));

            while (!stack.empty()) {
                var top = stack.peek();

                if (top.specialization == null) {
                    top.specialization = top.explainerInformation.specialization.iterator();
                }

                if (top.specialization.hasNext()) {
                    stack.push(new Frame(Objects.requireNonNull(explainers.get(top.specialization.next()))));
                    continue;
                }

                chain.add(stack.pop().explainerInformation.explainer);
            }

            if (chain.removeLast() != lastExplainer) throw new IllegalStateException();

            result = newExceptionMapper(lastExplainer, chain);
        }

        Preconditions.checkNotNull(result, "No hierarchy for %s is defined", exceptionClass);

        return result;
    }

    protected abstract M newExceptionMapper(
            StrictExceptionExplainer<E> lastExplainer, LinkedList<ExceptionExplainer<?>> chain);
}
