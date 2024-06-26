package org.zith.expr.ctxwl.core.reading.impl.readinginspiredlookup;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Optional;

public record ReadingInspiredLookupDocument(
        @BsonId
        Id id,
        String criterion,
        Long offset,
        Instant creationTime,
        Boolean unprocessed
) {
    public ObjectId historyEntryReference() {
        return Optional.ofNullable(id).map(Id::parent).orElse(null);
    }

    public Long serial() {
        return Optional.ofNullable(id).map(Id::serial).orElse(null);
    }

    public record Id(ObjectId parent, Long serial) {
    }

    public static final class Fields {
        public static final String id = "_id";
        public static final String creationTime = "creationTime";
        public static final String unprocessed = "unprocessed";

        private Fields() {
        }
    }
}
