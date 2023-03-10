package org.zith.expr.ctxwl.common.wordnet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MorphologyTest {
    private static MorphologyExceptionDatabase adjectiveExceptions;
    private static MorphologyExceptionDatabase adverbExceptions;
    private static MorphologyExceptionDatabase nounExceptions;
    private static MorphologyExceptionDatabase verbExceptions;
    private static IndexDatabase adjectiveIndex;
    private static IndexDatabase adverbIndex;
    private static IndexDatabase nounIndex;
    private static IndexDatabase verbIndex;
    private static Morphology morphology;


    @BeforeAll
    public static void setup() {
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

    @AfterAll
    public static void clean() throws IOException {
        adjectiveIndex.close();
        adverbIndex.close();
        nounIndex.close();
        verbIndex.close();
    }

    @Test
    public void testBetter() {
        var result = Set.of(
                new WordAsPartOfSpeech("better", PartOfSpeech.Noun),
                new WordAsPartOfSpeech("well", PartOfSpeech.Adjective),
                new WordAsPartOfSpeech("better", PartOfSpeech.Adjective),
                new WordAsPartOfSpeech("good", PartOfSpeech.Adjective),
                new WordAsPartOfSpeech("better", PartOfSpeech.Verb),
                new WordAsPartOfSpeech("well", PartOfSpeech.Adverb));
        assertEquals(result, morphology.getBaseForms("better"));
    }

    @Test
    public void testPotatoes() {
        var result = Set.of(new WordAsPartOfSpeech("potato", PartOfSpeech.Noun));
        assertEquals(result, morphology.getBaseForms("potatoes"));
    }

    @Test
    public void testChildren() {
        var result = Set.of(new WordAsPartOfSpeech("child", PartOfSpeech.Noun));
        assertEquals(result, morphology.getBaseForms("children"));
    }

    @Test
    public void testActresses() {
        var result = Set.of(new WordAsPartOfSpeech("actress", PartOfSpeech.Noun));
        assertEquals(result, morphology.getBaseForms("actresses"));
    }

    @Test
    public void testBeing() {
        var result = Set.of(new WordAsPartOfSpeech("being", PartOfSpeech.Noun),
                new WordAsPartOfSpeech("be", PartOfSpeech.Verb));
        assertEquals(result, morphology.getBaseForms("being"));
    }

    @Test
    public void testIs() {
        var result = Set.of(new WordAsPartOfSpeech("be", PartOfSpeech.Verb));
        assertEquals(result, morphology.getBaseForms("is"));
    }
}