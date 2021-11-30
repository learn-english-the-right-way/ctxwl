package org.zith.expr.ctxwl.common.mongodb;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

import java.util.function.Supplier;

public class MongoDbConfiguration {
    private final Content content;
    private final Supplier<ConnectionString> connectionStringSupplier;

    public MongoDbConfiguration(String uri) {
        Preconditions.checkNotNull(uri);
        this.content = new Content(uri);
        connectionStringSupplier = Suppliers.memoize(() -> new ConnectionString(content.uri()));
    }

    public String uri() {
        return content.uri();
    }

    public ConnectionString connectionString() {
        return connectionStringSupplier.get();
    }

    public MongoClientSettings makeMongoClientSettings() {
        return MongoClientSettings.builder()
                .applyConnectionString(connectionString())
                .build();
    }

    private static record Content(String uri) {
    }
}
