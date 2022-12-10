package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.accesscontrol.ActiveResourceRole;
import org.zith.expr.ctxwl.webapi.accesscontrol.ApplicationKeyRole;
import org.zith.expr.ctxwl.webapi.accesscontrol.Principal;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyPrincipal;
import org.zith.expr.ctxwl.webapi.endpoint.readingsession.ReadingSessionWebCollection;

import java.util.List;
import java.util.Objects;

@Path("/reading_history_entry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ReadingHistoryEntryWebCollection {

    private final ReadingService readingService;
    private final SecurityContext securityContext;

    @Inject
    public ReadingHistoryEntryWebCollection(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    @Path("{group}-{serial}")
    @PUT
    public ReadingHistoryEntryWebDocument create(
            @PathParam("group") String group,
            @PathParam("serial") Long serial,
            ReadingHistoryEntryWebDocument document) throws Exception {
        var optionalApplicationKey = CtxwlKeyPrincipal.resolveDelegate(securityContext.getUserPrincipal()).stream()
                .map(Principal::roles)
                .flatMap(List::stream)
                .filter(ActiveResourceRole.match(CredentialManager.ResourceType.USER))
                .map(ActiveResourceRole.class::cast)
                .findFirst()
                .flatMap(ActiveResourceRole::optionalApplicationKeyRole)
                .map(ApplicationKeyRole::applicationKey);

        if (optionalApplicationKey.isEmpty()) {
            throw new ReadingHistoryException.InvalidCredentialException();
        }

        var applicationKey = optionalApplicationKey.get();

        if (!Objects.equals(ReadingSessionWebCollection.escape(applicationKey), group)) {
            throw new ReadingHistoryException.UnauthorizedAccessToSessionException();
        }

        var optionalReadingSession = readingService.loadSession(group, serial);

        if (optionalReadingSession.isEmpty()) {
            throw new ReadingHistoryException.SessionNotFoundException();
        }

        var readingSession = optionalReadingSession.get();

        var result = readingSession.create(
                document.serial(),
                new ReadingHistoryEntryValue(
                        document.uri(),
                        document.text(),
                        document.creationTime(),
                        document.updateTime(),
                        document.majorSerial()
                ));
        return new ReadingHistoryEntryWebDocument(
                result.serial(),
                result.uri(),
                result.text(),
                result.creationTime(),
                result.updateTime(),
                result.majorSerial()
        );
    }

}
