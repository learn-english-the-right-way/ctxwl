package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.core.accesscontrol.ActiveResourceRole;
import org.zith.expr.ctxwl.core.accesscontrol.ApplicationKeyRole;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyPrincipal;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Path("/reading_session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ReadingSessionWebCollection {

    private final ReadingService readingService;
    private final SecurityContext securityContext;

    @Inject
    public ReadingSessionWebCollection(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    @POST
    public ReadingSessionWebDocument create(ReadingSessionWebDocument document) throws Exception {
        var optionalApplicationKey = CtxwlKeyPrincipal.resolveDelegate(securityContext.getUserPrincipal()).stream()
                .map(Principal::roles)
                .flatMap(List::stream)
                .filter(ActiveResourceRole.match(CredentialManager.ResourceType.USER))
                .map(ActiveResourceRole.class::cast)
                .findFirst()
                .flatMap(ActiveResourceRole::optionalApplicationKeyRole)
                .map(ApplicationKeyRole::applicationKey);

        if (optionalApplicationKey.isEmpty()) {
            throw new ForbiddenException();
        }

        var applicationKey = optionalApplicationKey.get();

        // TODO avoid exposing credentials in URLs
        try (var readingSession = readingService.makeSession(escape(applicationKey))) {
            return makeWebDocument(readingSession);
        }
    }

    @NotNull
    private ReadingSessionWebDocument makeWebDocument(ReadingSession readingSession) {
        return new ReadingSessionWebDocument(
                "%s-%d".formatted(readingSession.getGroup(), readingSession.getSerial()),
                // TODO deal with fractions
                readingSession.getUpdateTime().map(Instant::getEpochSecond).orElse(null),
                readingSession.getCompletionTime().map(Instant::getEpochSecond).orElse(null),
                readingSession.getTerminationTime().map(Instant::getEpochSecond).orElse(null)
        );
    }

    public static String escape(String applicationKey) {
        return applicationKey.chars()
                .flatMap(ch -> {
                    if (Character.isLetterOrDigit(ch)) {
                        return IntStream.of(ch);
                    } else {
                        var hex = Integer.toHexString(ch);
                        return Stream.of(IntStream.of('_'), Integer.toHexString(hex.length()).chars(), hex.chars())
                                .reduce(IntStream::concat)
                                .orElseGet(IntStream::empty);
                    }
                })
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
