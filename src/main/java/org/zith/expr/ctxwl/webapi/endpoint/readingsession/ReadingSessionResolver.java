package org.zith.expr.ctxwl.webapi.endpoint.readingsession;

import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.accesscontrol.ActiveResourceRole;
import org.zith.expr.ctxwl.core.accesscontrol.ApplicationKeyRole;
import org.zith.expr.ctxwl.core.accesscontrol.Principal;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.ReadingSession;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyPrincipal;

import java.util.List;
import java.util.Objects;

public class ReadingSessionResolver {
    private final ReadingService readingService;
    private final SecurityContext securityContext;

    public ReadingSessionResolver(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    public ReadingSession resolve(String group, long serial) {
        Objects.requireNonNull(group);

        var optionalApplicationKey = CtxwlKeyPrincipal.resolveDelegate(securityContext.getUserPrincipal()).stream()
                .map(Principal::roles)
                .flatMap(List::stream)
                .filter(ActiveResourceRole.match(CredentialManager.ResourceType.USER))
                .map(ActiveResourceRole.class::cast)
                .findFirst()
                .flatMap(ActiveResourceRole::optionalApplicationKeyRole)
                .map(ApplicationKeyRole::applicationKey);

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
