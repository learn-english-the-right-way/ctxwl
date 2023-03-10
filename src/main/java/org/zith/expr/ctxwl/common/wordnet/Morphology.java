package org.zith.expr.ctxwl.common.wordnet;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Morphology {
    private final Transformation[] transformations = new Transformation[]{
            suffix(PartOfSpeech.Noun, "s", ""),
            suffix(PartOfSpeech.Noun, "ses", "s"),
            suffix(PartOfSpeech.Noun, "xes", "x"),
            suffix(PartOfSpeech.Noun, "zes", "z"),
            suffix(PartOfSpeech.Noun, "ches", "ch"),
            suffix(PartOfSpeech.Noun, "shes", "sh"),
            suffix(PartOfSpeech.Noun, "men", "man"),
            suffix(PartOfSpeech.Noun, "ies", "y"),
            suffix(PartOfSpeech.Verb, "s", ""),
            suffix(PartOfSpeech.Verb, "ies", "y"),
            suffix(PartOfSpeech.Verb, "es", "e"),
            suffix(PartOfSpeech.Verb, "es", ""),
            suffix(PartOfSpeech.Verb, "ed", "e"),
            suffix(PartOfSpeech.Verb, "ed", ""),
            suffix(PartOfSpeech.Verb, "ing", "e"),
            suffix(PartOfSpeech.Verb, "ing", ""),
            suffix(PartOfSpeech.Adjective, "er", ""),
            suffix(PartOfSpeech.Adjective, "est", ""),
            suffix(PartOfSpeech.Adjective, "er", "e"),
            suffix(PartOfSpeech.Adjective, "est", "e")};
    private final MorphologyExceptionDatabase adjectiveExceptions;
    private final MorphologyExceptionDatabase adverbExceptions;
    private final MorphologyExceptionDatabase nounExceptions;
    private final MorphologyExceptionDatabase verbExceptions;
    private final IndexDatabase adjectiveIndex;
    private final IndexDatabase adverbIndex;
    private final IndexDatabase nounIndex;
    private final IndexDatabase verbIndex;

    Morphology(
            MorphologyExceptionDatabase adjectiveExceptions,
            MorphologyExceptionDatabase adverbExceptions,
            MorphologyExceptionDatabase nounExceptions,
            MorphologyExceptionDatabase verbExceptions,
            IndexDatabase adjectiveIndex,
            IndexDatabase adverbIndex,
            IndexDatabase nounIndex,
            IndexDatabase verbIndex
    ) {
        this.adjectiveExceptions = adjectiveExceptions;
        this.adverbExceptions = adverbExceptions;
        this.nounExceptions = nounExceptions;
        this.verbExceptions = verbExceptions;
        this.adjectiveIndex = adjectiveIndex;
        this.adverbIndex = adverbIndex;
        this.nounIndex = nounIndex;
        this.verbIndex = verbIndex;
    }

    Set<WordAsPartOfSpeech> getBaseForms(String word) {
        return Stream.of(
                        Stream.concat(Stream.of(word), Optional.of(adjectiveExceptions.getBaseForms(word))
                                        .filter(r -> !r.isEmpty())
                                        .map(Set::stream)
                                        .orElseGet(() -> Arrays.stream(transformations)
                                                .filter(t -> t.partOfSpeech() == PartOfSpeech.Adjective)
                                                .map(t -> t.revert(word))
                                                .flatMap(Optional::stream)))
                                .map(adjectiveIndex::lookup)
                                .flatMap(Optional::stream)
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Adjective)),
                        Stream.concat(Stream.of(word), Optional.of(adverbExceptions.getBaseForms(word))
                                        .filter(r -> !r.isEmpty())
                                        .map(Set::stream)
                                        .orElseGet(() -> Arrays.stream(transformations)
                                                .filter(t -> t.partOfSpeech() == PartOfSpeech.Adverb)
                                                .map(t -> t.revert(word))
                                                .flatMap(Optional::stream)))
                                .map(adverbIndex::lookup)
                                .flatMap(Optional::stream)
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Adverb)),
                        Stream.concat(Stream.of(word), Optional.of(nounExceptions.getBaseForms(word))
                                        .filter(r -> !r.isEmpty())
                                        .map(Set::stream)
                                        .orElseGet(() -> Arrays.stream(transformations)
                                                .filter(t -> t.partOfSpeech() == PartOfSpeech.Noun)
                                                .map(t -> t.revert(word))
                                                .flatMap(Optional::stream)))
                                .map(nounIndex::lookup)
                                .flatMap(Optional::stream)
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Noun)),
                        Stream.concat(Stream.of(word), Optional.of(verbExceptions.getBaseForms(word))
                                        .filter(r -> !r.isEmpty())
                                        .map(Set::stream)
                                        .orElseGet(() -> Arrays.stream(transformations)
                                                .filter(t -> t.partOfSpeech() == PartOfSpeech.Verb)
                                                .map(t -> t.revert(word))
                                                .flatMap(Optional::stream)))
                                .map(verbIndex::lookup)
                                .flatMap(Optional::stream)
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Verb)))
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
    }

    private Stream<WordAsPartOfSpeech> streamExceptionalBaseForms(String word) {
        return Stream.of(
                        adjectiveExceptions.getBaseForms(word).stream()
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Adjective)),
                        adverbExceptions.getBaseForms(word).stream()
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Adverb)),
                        nounExceptions.getBaseForms(word).stream()
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Noun)),
                        verbExceptions.getBaseForms(word).stream()
                                .map(w -> new WordAsPartOfSpeech(w, PartOfSpeech.Verb)))
                .flatMap(Function.identity());
    }

    private static Transformation suffix(PartOfSpeech partOfSpeech, String suffix, String ending) {
        return new Transformation(partOfSpeech, suffix, ending);
    }

    private record Transformation(PartOfSpeech partOfSpeech, String suffix, String ending) {
        Optional<String> revert(String word) {
            if (word.endsWith(suffix())) {
                return Optional.of(word.substring(0, word.length() - suffix().length()) + ending());
            } else {
                return Optional.empty();
            }
        }
    }

}
