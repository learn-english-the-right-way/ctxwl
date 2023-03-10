package org.zith.expr.ctxwl.common.wordnet;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexDatabaseTest {
    @Test
    public void test1() throws IOException {
        try (var database = new IndexDatabase("index.adj")) {
            assertEquals(Optional.of("good"), database.lookup("good"));
        }
    }
}