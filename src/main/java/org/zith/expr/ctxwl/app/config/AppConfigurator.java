package org.zith.expr.ctxwl.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@NotThreadSafe
public final class AppConfigurator {
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private AppConfiguration current = AppConfiguration.empty();

    private AppConfigurator() {
    }

    public static AppConfigurator create() {
        return new AppConfigurator();
    }

    public void load(File file) {
        AppConfiguration configuration;
        try {
            configuration = objectMapper.readValue(file, AppConfiguration.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        current = current.merge(configuration);
    }

    public AppConfiguration configuration() {
        return current;
    }
}
