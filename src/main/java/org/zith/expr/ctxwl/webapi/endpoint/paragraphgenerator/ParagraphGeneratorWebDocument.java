package org.zith.expr.ctxwl.webapi.endpoint.paragraphgenerator;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ParagraphGeneratorWebDocument(
        String keyword,
        String content
) {}