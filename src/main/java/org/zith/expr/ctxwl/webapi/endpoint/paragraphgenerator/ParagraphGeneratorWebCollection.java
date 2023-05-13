package org.zith.expr.ctxwl.webapi.endpoint.paragraphgenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.app.config.core.paragraphgenerator.AppCoreParagraphGeneratorConfiguration;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlPrincipal;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Path("/generated_paragraph")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ParagraphGeneratorWebCollection {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenAICompletionResponse(
            List<Choice> choices
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Choice (
                Message message
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            record Message(
                    String content
            ) {}
        }
    }

    record OpenAICompletionRequest(
            String model,
            List<OpenAICompletionRequestMessage> messages
    ) {
        record OpenAICompletionRequestMessage(
                String role,
                String content
        ) {}
    }

    private final ReadingService readingService;

    private final SecurityContext securityContext;

    private final AppCoreParagraphGeneratorConfiguration config;

    @Inject
    public ParagraphGeneratorWebCollection(ReadingService readingService, SecurityContext securityContext, AppCoreParagraphGeneratorConfiguration config) {
        this.readingService = readingService;
        this.securityContext = securityContext;
        this.config = config;
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



        var completion = new CompletableFuture<Void>();
        var data = new LinkedList<ReadingEvent>();
        readingService.collect(ForkJoinPool.commonPool()).subscribe(new Flow.Subscriber<>() {

            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Tracked<ReadingEvent> item) {
                data.add(item.value());
                item.acknowledge();
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                completion.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completion.complete(null);
            }
        });

        completion.get();

        readingService.extendWordlist(data);

        var words = readingService.getWordlist(principal.name()).getWords();

        if (words.isEmpty()) {
            return null;
        } else {
            Random random = new Random();

            String randomWord = words.get(random.nextInt(words.size()));

            Client client = ClientBuilder.newClient();
            WebTarget completionService = client.target(config.openAI().url());

            var request = new OpenAICompletionRequest(
                    "gpt-3.5-turbo",
                    List.of(new OpenAICompletionRequest.OpenAICompletionRequestMessage("user", "Give me an interesting short story in simple language which is less than 150 words based on the word:" + randomWord))
            );

            Entity<OpenAICompletionRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);
            Invocation.Builder builder = completionService.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + config.openAI().apiKey());

            CompletableFuture<OpenAICompletionResponse> future = CompletableFuture.supplyAsync(() -> {
                Response response = builder.post(entity);
                System.out.println(response);
                OpenAICompletionResponse result = response.readEntity(OpenAICompletionResponse.class);
                System.out.println(result);
                return result;
            });

            OpenAICompletionResponse response = future.join();
            client.close();

            return new ParagraphGeneratorWebDocument(randomWord, response.choices().get(0).message.content);
        }
    }
}
