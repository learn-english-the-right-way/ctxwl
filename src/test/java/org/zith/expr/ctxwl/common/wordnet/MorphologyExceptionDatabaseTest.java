package org.zith.expr.ctxwl.common.wordnet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphologyExceptionDatabaseTest {
    @Test
    public void test1() {
        var database = new MorphologyExceptionDatabase("adj.exc");
        var bases = database.getBaseForms("better");
        assertTrue(bases.contains("good"));
        assertTrue(bases.contains("well"));
    }

    @Test
    public void test2() {
        var database = new MorphologyExceptionDatabase("verb.exc");
        var bases = database.getBaseForms("were");
        assertTrue(bases.contains("be"));
    }
}