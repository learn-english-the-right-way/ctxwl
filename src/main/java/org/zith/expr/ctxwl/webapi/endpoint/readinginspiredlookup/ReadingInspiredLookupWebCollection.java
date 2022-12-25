package org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookup;
import org.zith.expr.ctxwl.core.reading.ReadingInspiredLookupValue;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.endpoint.readingsession.ReadingSessionResolver;

import java.util.Objects;

@Path("/reading_inspired_lookup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ReadingInspiredLookupWebCollection {
    private final ReadingService readingService;
    private final SecurityContext securityContext;

    @Inject
    public ReadingInspiredLookupWebCollection(ReadingService readingService, SecurityContext securityContext) {
        this.readingService = readingService;
        this.securityContext = securityContext;
    }

    @Path("{sessionGroup}-{sessionSerial}-{entrySerial}-{serial}")
    @PUT
    public ReadingInspiredLookupWebDocument create(
            @PathParam("sessionGroup") String sessionGroup,
            @PathParam("sessionSerial") Long sessionSerial,
            @PathParam("entrySerial") Long entrySerial,
            @PathParam("serial") Long serial,
            ReadingInspiredLookupWebDocument document
    ) {
        if (!Objects.equals(document.session(), "%s-%d".formatted(sessionGroup, sessionSerial))) {
            throw new ReadingInspiredLookupException.FieldNotAcceptedException("session");
        }
        if (!(entrySerial != null && entrySerial >= 0 && Objects.equals(entrySerial, document.entrySerial()))) {
            throw new ReadingInspiredLookupException.FieldNotAcceptedException("entrySerial");
        }
        if (!(serial != null && serial >= 0 && Objects.equals(serial, document.serial()))) {
            throw new ReadingInspiredLookupException.FieldNotAcceptedException("serial");
        }
        if (document.creationTime().isEmpty()) {
            throw new ReadingInspiredLookupException.FieldNotAcceptedException("creationTime");
        }

        var readingSessionResolver = new ReadingSessionResolver(readingService, securityContext);
        ReadingInspiredLookup result;
        try (var readingSession = readingSessionResolver.resolve(sessionGroup, sessionSerial)) {
            result = readingSession.createLookup(
                    entrySerial,
                    serial,
                    new ReadingInspiredLookupValue(
                            document.criterion(),
                            document.offset(),
                            document.creationTime()
                    ));
        }

        return new ReadingInspiredLookupWebDocument(
                "%s-%d".formatted(result.session().getGroup(), result.session().getSerial()),
                result.historyEntrySerial(),
                result.serial(),
                result.criterion(),
                result.offset(),
                result.creationTime()
        );
    }
}
