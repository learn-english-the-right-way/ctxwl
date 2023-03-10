package org.zith.expr.ctxwl.common.wordnet;

import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class MorphologyExceptionDatabase {
    private final String filename;
    private final Supplier<Map<String, Set<String>>> mappingsSupplier;

    MorphologyExceptionDatabase(String filename) {
        this.filename = Objects.requireNonNull(filename);
        mappingsSupplier = Suppliers.memoize(this::loadMappings);
    }

    private Map<String, Set<String>> loadMappings() {
        try (var resource = Objects.requireNonNull(
                MorphologyExceptionDatabase.class.getResourceAsStream(Constants.RESOURCE_BASE + filename),
                "Failed to load the resource file")) {
            var reader = new BufferedReader(new InputStreamReader(resource));
            return reader.lines().flatMap(line -> {
                        if (line.isEmpty()) return Stream.empty();

                        var pos = line.indexOf(' ');
                        if (pos < 0) throw new IllegalArgumentException();
                        var inflection = line.substring(0, pos);
                        var bases = line.substring(pos + 1);
                        return Stream.of(Map.entry(inflection, Splitter.on(' ').split(bases)));
                    })
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(
                                    e -> StreamSupport.stream(e.getValue().spliterator(), false),
                                    Collectors.collectingAndThen(
                                            Collectors.reducing(Stream::concat),
                                            s -> s.stream()
                                                    .flatMap(Function.identity())
                                                    .collect(Collectors.toSet())))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Set<String> getBaseForms(String word) {
        return Optional.ofNullable(mappingsSupplier.get().get(word)).orElse(Set.of());
    }
}
