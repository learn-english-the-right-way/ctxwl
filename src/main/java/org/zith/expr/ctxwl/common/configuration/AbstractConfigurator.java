package org.zith.expr.ctxwl.common.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.app.config.AppConfigurator;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@NotThreadSafe
public abstract class AbstractConfigurator<T extends Configuration<T>> {
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private T current;

    protected AbstractConfigurator() {
    }

    public static AppConfigurator create() {
        return new AppConfigurator();
    }

    public void load(File file) {
        T configuration;
        try {
            configuration = objectMapper.readValue(file, configurationClass());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (current == null) {
            current = emptyConfiguration();
        }
        current = current.merge(configuration);
    }

    public T configuration() {
        return current;
    }

    @NotNull
    protected abstract T emptyConfiguration();

    protected abstract Class<T> configurationClass();
}
