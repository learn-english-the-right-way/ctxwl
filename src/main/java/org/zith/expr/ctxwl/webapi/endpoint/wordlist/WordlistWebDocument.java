package org.zith.expr.ctxwl.webapi.endpoint.wordlist;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record WordlistWebDocument(
    List<String> words
) {
}
