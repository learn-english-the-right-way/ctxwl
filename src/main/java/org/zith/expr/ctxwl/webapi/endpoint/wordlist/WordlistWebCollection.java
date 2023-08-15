package org.zith.expr.ctxwl.webapi.endpoint.wordlist;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.common.async.Tracked;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.reading.ReadingEvent;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlPrincipal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Path("/wordlist")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class WordlistWebCollection {
    private final ReadingService readingService;

    private final SecurityContext securityContext;
    @Inject
    public WordlistWebCollection(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    @GET
    public WordlistWebDocument read() throws Exception {
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

        var entries = readingService.getWordlist(principal.name()).getWords().stream()
                .map(WordlistWebDocument.Entry::new)
                .toList();

        return new WordlistWebDocument(entries);
    }

    @PUT
    public WordlistWebDocument update(WordlistWebDocument document) throws Exception {
        // get request wordlist
        var proposedWordlist = new HashSet<>(document.entries());

        // get existing wordlist
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

        // get words to delete
        var currentWordlist = readingService.getWordlist(principal.name()).getWords().stream()
                .map(WordlistWebDocument.Entry::new)
                .toList();
        var wordsToDelete = new HashSet<>(currentWordlist);
        // TODO: handle case where proposed wordlist contains word(s) not in current wordlist
        wordsToDelete.removeAll(proposedWordlist);

        // delete words from wordlist
        var iterator = wordsToDelete.iterator();
        var wordlist = readingService.getWordlist(principal.name());
        while (iterator.hasNext()) {
            wordlist.delete(iterator.next().word());
        }

        // return new wordlist
        var entries = readingService.getWordlist(principal.name()).getWords().stream()
                .map(WordlistWebDocument.Entry::new)
                .toList();
        return new WordlistWebDocument(entries);
    }
}
