package org.zith.expr.ctxwl.webapi.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class SimpleExceptionMapper<E extends Exception> implements ExceptionMapper<E> {

    private final Explainer<E> explainer;
    private final List<Explainer<?>> finerExplainer;
    private final List<Explainer<?>> coarserExplainer;

    protected SimpleExceptionMapper(
            Explainer<E> explainer,
            List<Explainer<?>> finerExplainers,
            List<Explainer<?>> coarserExplainers
    ) {
        this.explainer = explainer;
        finerExplainer = List.copyOf(finerExplainers);
        coarserExplainer = List.copyOf(coarserExplainers);
    }

    @Override
    public final Response toResponse(E exception) {
        var cause = explainer.explain(exception);
        var finerTranslations =
                finerExplainer.stream()
                        .flatMap(explainer -> Explainer.explain(explainer, exception).stream())
                        .toList();
        var coarserTranslations =
                coarserExplainer.stream()
                        .flatMap(explainer -> Explainer.explain(explainer, exception).stream())
                        .toList();
        var specific = finerTranslations.stream().findFirst().orElse(cause);
        return Response.status(specific.code().status())
                .entity(new ExceptionDescriptor(
                        Stream.of(
                                        finerTranslations.stream(),
                                        Stream.of(cause),
                                        coarserTranslations.stream()
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

    public interface Explainer<E extends Exception> {
        Class<E> exceptionClass();

        Cause explain(E exception);

        static <B extends Exception, E extends Exception> Optional<Cause> explain(Explainer<B> explainer, E exception) {
            var exceptionClass = explainer.exceptionClass();
            if (!exceptionClass.isInstance(exception)) return Optional.empty();
            return Optional.of(explainer.explain(exceptionClass.cast(exception)));
        }
    }

    public interface Cause {
        Code code();

        String message();

        @Nullable
        Object payload();
    }

    public interface Code {
        String component();

        String name();

        Response.StatusType status();
    }

    public static final class SimpleCause implements Cause {

        private final Supplier<String> message;
        private final Code code;
        private final @Nullable Object payload;

        private SimpleCause(Code code, Supplier<String> message, @Nullable Object payload) {
            Objects.requireNonNull(message);
            Objects.requireNonNull(code);
            this.message = message;
            this.code = code;
            this.payload = payload;
        }

        @Override
        public Code code() {
            return code;
        }

        @Override
        public String message() {
            return message.get();
        }

        @Override
        public @Nullable Object payload() {
            return payload;
        }

        public SimpleCause payload(@Nullable Object payload) {
            return new SimpleCause(code, message, payload);
        }

        public static SimpleCause create(Code code, String message) {
            return new SimpleCause(code, () -> message, null);
        }

        public static SimpleCause create(Code code, String messageFormat, Object... args) {
            return new SimpleCause(code, () -> messageFormat.formatted(args), null);
        }
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
