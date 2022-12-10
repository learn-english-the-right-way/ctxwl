package org.zith.expr.ctxwl.webapi.mapper.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractExceptionMapper<E extends Exception> implements ExceptionMapper<E> {

    private final StrictExceptionExplainer<E> lastExplainer;
    private final List<ExceptionExplainer<?>> explainers;

    protected AbstractExceptionMapper(
            StrictExceptionExplainer<E> lastExplainer,
            List<ExceptionExplainer<?>> explainers
    ) {
        Objects.requireNonNull(lastExplainer);

        this.lastExplainer = lastExplainer;
        this.explainers = List.copyOf(explainers);
    }

    @Override
    public final Response toResponse(E exception) {
        var roughCause = lastExplainer.explain(exception);
        var finerTranslations =
                explainers.stream()
                        .flatMap(explainer -> ExceptionExplainer.explain(explainer, exception).stream())
                        .toList();
        var specific = finerTranslations.stream().findFirst().orElse(roughCause);
        return Response.status(specific.code().status())
                .entity(new ExceptionDescriptor(
                        Stream.of(
                                        finerTranslations.stream(),
                                        Stream.of(roughCause)
                                )
                                .reduce(Stream::concat)
                                .stream()
                                .flatMap(Function.identity())
                                .map(t -> new ExceptionDescriptor.Cause(
                                        t.code().component(),
                                        t.code().name(),
                                        t.payload()))
                                .toList(),
                        specific.message()
                ))
                .build();
    }

    public interface Code {
        String component();

        String name();

        Response.StatusType status();
    }

    private record ExceptionDescriptor(
            List<Cause> causes,
            String message
    ) {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Cause(
                String component,
                String code,
                @Nullable Object payload
        ) {
        }
    }
}
