package org.zith.expr.ctxwl.webapi.endpoint.paragraphgenerator;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ParagraphGeneratorWebDocument(
        List<Paragraph> paragraphs
) {
    record Paragraph (
            String keyword,
            String content
    ) {}
}