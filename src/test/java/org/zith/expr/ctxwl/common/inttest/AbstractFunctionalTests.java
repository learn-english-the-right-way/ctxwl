package org.zith.expr.ctxwl.common.inttest;

import com.google.common.base.CaseFormat;
import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.zith.expr.ctxwl.common.close.CombinedAutoCloseable;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Tag("functional")
public abstract class AbstractFunctionalTests<C> {
    protected static final CombinedAutoCloseable combinedAutoCloseable = CombinedAutoCloseable.create();

    @AfterAll
    public static void clean() {
        combinedAutoCloseable.close();
    }

    private static final String CONFIGURATION_PATH_PROPERTY = "org.zith.expr.ctxwl.common.functest.configuration.path";
    private static final String CONFIGURATION_PATH_ENV = "CTXWL_FUNCTEST_CONFIG";
    private final Supplier<C> configuration;

    protected AbstractFunctionalTests() {
        configuration = Suppliers.memoize(this::readConfiguration);
    }

    protected final C configuration() {
        return configuration.get();
    }

    @NotNull
    private C readConfiguration() {
        var maybeConfigurationPath = Stream.<Supplier<Optional<String>>>of(
                        () -> Optional.ofNullable(System.getProperty(CONFIGURATION_PATH_PROPERTY)),
                        () -> Optional.ofNullable(System.getenv().get(CONFIGURATION_PATH_ENV)))
                .<Supplier<Optional<String>>>map(s -> () -> s.get().filter(v -> !v.isEmpty()))
                .reduce((v1, v2) -> () -> v1.get().or(v2))
                .flatMap(Supplier::get);

        if (maybeConfigurationPath.isEmpty()) {
            throw new IllegalArgumentException(
                    "The configuration path isn't specified. Please specify the configuration path with JVM option " +
                            "\"-D" + CONFIGURATION_PATH_PROPERTY + "=<path>\" or environment variable " +
                            "\"" + CONFIGURATION_PATH_ENV + "\"");
        }

        var configurationPath = maybeConfigurationPath.get();

        var configurationDirectory = new File(configurationPath);

        if (!configurationDirectory.exists()) {
            throw new IllegalArgumentException("The configuration path doesn't exist");
        }

        if (!configurationDirectory.isDirectory()) {
            throw new IllegalArgumentException("The configuration path isn't a directory");
        }

        if (!configurationDirectory.canRead()) {
            throw new IllegalArgumentException("The configuration path cannot be read by the tests");
        }

        var configurationNames = configurationNames();
        var configurationFiles =
                configurationNames.stream()
                        .map(n -> new File(configurationDirectory, n))
                        .filter(File::exists)
                        .toArray(File[]::new);

        if (configurationFiles.length <= 0) {
            throw new IllegalArgumentException(
                    "None of configuration files has been found - candidates: " +
                            String.join(", ", configurationNames));
        }

        return parseConfiguration(configurationFiles);
    }

    @NotNull
    protected abstract C parseConfiguration(File... files);

    @NotNull
    protected List<String> configurationNames() {
        var converter = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);
        var trimmingPattens = List.of(Pattern.compile("(:?Abstract)?(?<name>[\\p{Upper}].*)"));
        return Stream.<Optional<Class<?>>>iterate(
                        Optional.of(getClass()),
                        Optional::isPresent,
                        v -> v.flatMap(c -> Optional.ofNullable(c.getSuperclass())))
                .flatMap(Optional::stream)
                .takeWhile(c -> c != AbstractFunctionalTests.class)
                .map(Class::getSimpleName)
                .map(n -> trimmingPattens.stream()
                        .map(v -> v.matcher(n))
                        .filter(Matcher::matches)
                        .flatMap(v -> Optional.ofNullable(v.group("name")).stream())
                        .findFirst()
                        .orElse(n))
                .map(converter::convert)
                .distinct()
                .map(n -> n + ".yaml")
                .toList();
    }
}
