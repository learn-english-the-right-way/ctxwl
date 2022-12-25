package org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntry;
import org.zith.expr.ctxwl.core.reading.ReadingHistoryEntryValue;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.endpoint.readingsession.ReadingSessionResolver;

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

    @Path("{sessionGroup}-{sessionSerial}-{serial}")
    @PUT
    public ReadingHistoryEntryWebDocument create(
            @PathParam("sessionGroup") String sessionGroup,
            @PathParam("sessionSerial") Long sessionSerial,
            @PathParam("serial") Long serial,
            ReadingHistoryEntryWebDocument document) throws Exception {
        if (!Objects.equals(document.session(), "%s-%d".formatted(sessionGroup, sessionSerial))) {
            throw new ReadingHistoryException.FieldNotAcceptedException("session");
        }
        if (!(serial != null && serial >= 0 && Objects.equals(serial, document.serial()))) {
            throw new ReadingHistoryException.FieldNotAcceptedException("serial");
        }
        if (document.creationTime().isEmpty()) {
            throw new ReadingHistoryException.FieldNotAcceptedException("creationTime");
        }

        var readingSessionResolver = new ReadingSessionResolver(readingService, securityContext);
        ReadingHistoryEntry result;
        try (var readingSession = readingSessionResolver.resolve(sessionGroup, sessionSerial)) {
            result = readingSession.createHistoryEntry(
                    serial,
                    new ReadingHistoryEntryValue(
                            document.uri(),
                            document.text(),
                            document.creationTime(),
                            document.updateTime(),
                            document.majorSerial()
                    ));
        }

        return new ReadingHistoryEntryWebDocument(
                "%s-%d".formatted(result.session().getGroup(), result.session().getSerial()),
                result.serial(),
                result.uri(),
                result.text(),
                result.creationTime(),
                result.updateTime(),
                result.majorSerial()
        );
    }

}
