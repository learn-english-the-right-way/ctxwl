package org.zith.expr.ctxwl.webapi.error;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.mapper.exception.AbstractExceptionMapper;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionCauseExplanation;
import org.zith.expr.ctxwl.webapi.mapper.exception.ExceptionExplainer;
import org.zith.expr.ctxwl.webapi.mapper.exception.StrictExceptionExplainer;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractExceptionExplainerMaker<C extends ErrorCode, B extends Exception> {
    protected abstract Response.StatusType status(C code);

    public <E extends B> StrictExceptionExplainer<E> make(
            C code, Class<E> exceptionClass, StrictCauseMaker<E> causeMaker) {
        return new SimpleStrictExplainer<>(
                new CodeAdapter(code, status(code)), exceptionClass, causeMaker);
    }

    public <E extends B> ExceptionExplainer<E> makeConditionalExplainer(
            C code, Class<E> exceptionClass, CauseMaker<E> causeMaker) {
        return new SimpleExplainer<>(
                new CodeAdapter(code, status(code)), exceptionClass, causeMaker);
    }

    public interface CauseMaker<E extends Exception> {
        Optional<? extends ExceptionCauseExplanation> makeCauseIfPossible(AbstractExceptionMapper.Code code, E exception);
    }

    public interface StrictCauseMaker<E extends Exception> extends CauseMaker<E> {
        ExceptionCauseExplanation makeCause(AbstractExceptionMapper.Code code, E exception);

        @Override
        default Optional<? extends ExceptionCauseExplanation> makeCauseIfPossible(
                AbstractExceptionMapper.Code code, E exception) {
            return Optional.of(makeCause(code, exception));
        }
    }

    private static class SimpleExplainer<E extends Exception> implements ExceptionExplainer<E> {

        protected final CodeAdapter code;
        protected final Class<E> exceptionClass;
        protected final CauseMaker<E> causeMaker;

        SimpleExplainer(
                CodeAdapter code,
                Class<E> exceptionClass,
                CauseMaker<E> causeMaker
        ) {
            Objects.requireNonNull(code);
            Objects.requireNonNull(exceptionClass);
            Objects.requireNonNull(causeMaker);
            this.code = code;
            this.exceptionClass = exceptionClass;
            this.causeMaker = causeMaker;
        }

        @Override
        public Class<E> exceptionClass() {
            return exceptionClass;
        }

        @Override
        public Optional<ExceptionCauseExplanation> explainIfPossible(E exception) {
            return causeMaker.makeCauseIfPossible(code, exception).map(Function.identity());
        }

        @Override
        public String toString() {
            return "Explainer{" +
                    "code=[" + code.component() + ":" + code.name() + "/" + code.status() + "]" +
                    ", exceptionClass=" + exceptionClass +
                    '}';
        }
    }

    private static class SimpleStrictExplainer<E extends Exception> extends SimpleExplainer<E> implements StrictExceptionExplainer<E> {

        private final StrictCauseMaker<E> strictCauseMaker;

        private SimpleStrictExplainer(
                CodeAdapter code,
                Class<E> exceptionClass,
                StrictCauseMaker<E> causeMaker
        ) {
            super(code, exceptionClass, causeMaker);
            Objects.requireNonNull(exceptionClass);
            this.strictCauseMaker = causeMaker;
        }

        @Override
        public ExceptionCauseExplanation explain(E exception) {
            return strictCauseMaker.makeCause(code, exception);
        }
    }

    private static final class CodeAdapter implements AbstractExceptionMapper.Code {
        private final ErrorCode value;
        private final Response.StatusType status;

        private CodeAdapter(ErrorCode value, Response.StatusType status) {
            this.value = value;
            this.status = status;
        }

        @Override
        public String component() {
            return value.component().identifier();
        }

        @Override
        public String name() {
            return value.representation();
        }

        @Override
        public Response.StatusType status() {
            return status;
        }
    }
}