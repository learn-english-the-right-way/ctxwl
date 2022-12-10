package org.zith.expr.ctxwl.webapi.mapper.exception;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class SimpleExceptionCauseExplanation implements ExceptionCauseExplanation {

    private final Supplier<String> message;
    private final AbstractExceptionMapper.Code code;
    private final @Nullable Object payload;

    private SimpleExceptionCauseExplanation(AbstractExceptionMapper.Code code, Supplier<String> message, @Nullable Object payload) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(code);
        this.message = message;
        this.code = code;
        this.payload = payload;
    }

    @Override
    public AbstractExceptionMapper.Code code() {
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

    public SimpleExceptionCauseExplanation payload(@Nullable Object payload) {
        return new SimpleExceptionCauseExplanation(code, message, payload);
    }

    public static SimpleExceptionCauseExplanation create(AbstractExceptionMapper.Code code, String message) {
        return new SimpleExceptionCauseExplanation(code, () -> message, null);
    }

    public static SimpleExceptionCauseExplanation create(AbstractExceptionMapper.Code code, String messageFormat, Object... args) {
        return new SimpleExceptionCauseExplanation(code, () -> messageFormat.formatted(args), null);
    }
}
