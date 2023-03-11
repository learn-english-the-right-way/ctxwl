package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyPrincipal;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class ReadingSessionResolver {
    private final ReadingService readingService;
    private final SecurityContext securityContext;

    public ReadingSessionResolver(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    public ReadingSession resolve(String group, long serial) {
        Objects.requireNonNull(group);

        var optionalApplicationKey =
                Stream.of(securityContext.getUserPrincipal())
                        .filter(CtxwlKeyPrincipal.class::isInstance)
                        .map(CtxwlKeyPrincipal.class::cast)
                        .flatMap(p -> p.getCompositingPrincipal(ControlledResourceType.USER).stream())
                        .map(Principal::applicationKeys)
                        .flatMap(Collection::stream)
                        .findFirst();

        if (optionalApplicationKey.isEmpty()) {
            throw new ReadingSessionException.InvalidCredentialException();
        }

        var applicationKey = optionalApplicationKey.get();

        if (!Objects.equals(ReadingSessionWebCollection.escape(applicationKey), group)) {
            throw new ReadingSessionException.UnauthorizedAccessToSessionException();
        }

        var optionalReadingSession = readingService.loadSession(group, serial);

        if (optionalReadingSession.isEmpty()) {
            throw new ReadingSessionException.SessionNotFoundException();
        }

        return optionalReadingSession.get();
    }
}
