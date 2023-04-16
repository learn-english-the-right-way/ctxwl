package org.zith.expr.ctxwl.webapi.endpoint.paragraphgenerator;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlPrincipal;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/generated_paragraph")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ParagraphGeneratorWebCollection {

    class OpenAICompletionResponse {
        class Choice {
            String text;
            Integer index;
            Integer logprobs;
            String finish_reason;
        }

        List<Choice> choices;
    }

    record OpenAICompletionRequest(
            String model,
            String prompt,
            Integer temperature,
            Integer n,
            Boolean stream
    ) {}

    private final ReadingService readingService;

    private final SecurityContext securityContext;

    @Inject
    public ParagraphGeneratorWebCollection(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    @GET
    public ParagraphGeneratorWebDocument create() throws Exception {
        var optionalPrincipal =
                Stream.of(securityContext.getUserPrincipal())
                        .filter(CtxwlPrincipal.class::isInstance)
                        .map(CtxwlPrincipal.class::cast)
                        .flatMap(p -> p.getCompositingPrincipal(ControlledResourceType.USER).stream())
                        .findFirst();

        if (optionalPrincipal.isEmpty()) {
            throw new ForbiddenException();
        }

        var principal = optionalPrincipal.get();

        var words = readingService.getWordlist(principal.name()).getWords();

        var requests = words.stream()
                .map(word -> new OpenAICompletionRequest(
                "gpt-3.5-turbo",
                "Give me an interesting story based on the word:" + word,
                1,
                1,
                false
                ))
                .toList();

        Client client = ClientBuilder.newClient();
        WebTarget completionService = client.target("https://api.openai.com/v1/completions");

        List<CompletableFuture<OpenAICompletionResponse>> futures = new ArrayList<>();
        for (OpenAICompletionRequest request : requests) {
            Entity<OpenAICompletionRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);
            Invocation.Builder builder = completionService.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer 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");
            CompletableFuture<OpenAICompletionResponse> future = CompletableFuture.supplyAsync(() -> {
                Response response = builder.post(entity);
                return response.readEntity(OpenAICompletionResponse.class);
            });
            futures.add(future);
        }
        client.close();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get();
        } catch (InterruptedException | ExecutionException e) {

        }

        List<OpenAICompletionResponse> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        var paragraphs = new ArrayList();
        for (int i = 0; i < results.size(); i++) {
            paragraphs.add(new ParagraphGeneratorWebDocument.Paragraph(
                    words.get(i),
                    results.get(i).choices.get(0).text
            ));
        }

        return new ParagraphGeneratorWebDocument(paragraphs.stream().toList());
    }
}
