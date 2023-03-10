package org.zith.expr.ctxwl.common.wordnet;

import java.util.ArrayList;
import java.util.Set;

public class WordNet implements AutoCloseable {

    private final MorphologyExceptionDatabase adjectiveExceptions;
    private final MorphologyExceptionDatabase adverbExceptions;
    private final MorphologyExceptionDatabase nounExceptions;
    private final MorphologyExceptionDatabase verbExceptions;
    private final IndexDatabase adjectiveIndex;
    private final IndexDatabase adverbIndex;
    private final IndexDatabase nounIndex;
    private final IndexDatabase verbIndex;
    private final Morphology morphology;

    public WordNet() {
        adjectiveExceptions = new MorphologyExceptionDatabase("adj.exc");
        adverbExceptions = new MorphologyExceptionDatabase("adv.exc");
        nounExceptions = new MorphologyExceptionDatabase("noun.exc");
        verbExceptions = new MorphologyExceptionDatabase("verb.exc");
        adjectiveIndex = new IndexDatabase("index.adj");
        adverbIndex = new IndexDatabase("index.adv");
        nounIndex = new IndexDatabase("index.noun");
        verbIndex = new IndexDatabase("index.verb");

        morphology = new Morphology(
                adjectiveExceptions,
                adverbExceptions,
                nounExceptions,
                verbExceptions,
                adjectiveIndex,
                adverbIndex,
                nounIndex,
                verbIndex
        );
    }

    public Set<WordAsPartOfSpeech> getBaseForms(String word) {
        return morphology.getBaseForms(word);
    }

    @Override
    public void close() {
        var throwables = new ArrayList<Throwable>(4);

        try {
            adjectiveIndex.close();
        } catch (Exception exception) {
            throwables.add(exception);
        }

        try {
            adverbIndex.close();
        } catch (Exception exception) {
            throwables.add(exception);
        }

        try {
            nounIndex.close();
        } catch (Exception exception) {
            throwables.add(exception);
        }

        try {
            verbIndex.close();
        } catch (Exception exception) {
            throwables.add(exception);
        }

        Exceptions.rethrow(throwables);
    }
}
