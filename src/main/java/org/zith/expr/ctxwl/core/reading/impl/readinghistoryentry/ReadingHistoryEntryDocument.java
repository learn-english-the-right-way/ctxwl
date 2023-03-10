package org.zith.expr.ctxwl.core.reading.impl.readinghistoryentry;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.zith.expr.ctxwl.core.reading.impl.readingsession.ReadingSessionKeyDocument;

import java.time.Instant;

public record ReadingHistoryEntryDocument(
        ReadingSessionKeyDocument session,
        Long serial,
        String uri,
        String text,
        Instant creationTime,
        Instant updateTime,
        Long majorSerial,
        Boolean unprocessed,
        @BsonId
        ObjectId id
) {
    public static final class Fields {
        public static final String id = "_id";
        public static final String session_group = "session.group";
        public static final String session_serial = "session.serial";
        public static final String serial = "serial";
        public static final String creationTime = "creationTime";
        public static final String updateTime = "updateTime";
        public static final String unprocessed = "unprocessed";

        private Fields() {
        }
    }
}
