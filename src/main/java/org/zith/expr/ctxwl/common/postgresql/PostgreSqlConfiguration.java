package org.zith.expr.ctxwl.common.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public final class PostgreSqlConfiguration {
    private final Content content;

    public PostgreSqlConfiguration(
            String uri,
            String username,
            String password
    ) {
        this.content = makeContent(uri, username, password);
    }

    public String uri() {
        return content.uri();
    }

    public String username() {
        return content.username();
    }

    public String password() {
        return content.password();
    }

    public DataSource makeDataSource(TransactionIsolation transactionIsolation) {
        var config = new HikariConfig();
        config.setDataSourceClassName(PGSimpleDataSource.class.getName());
        config.addDataSourceProperty("url", uri());
        config.setUsername(username());
        config.setPassword(password());
        config.setTransactionIsolation(transactionIsolation.name);
        return new HikariDataSource(config);
    }

    private static Content makeContent(String uri, String username, String password) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        if (!uri.startsWith("jdbc:postgresql:")) throw new IllegalArgumentException();
        if (username.isEmpty()) throw new IllegalArgumentException();
        if (password.isEmpty()) throw new IllegalArgumentException();
        return new Content(uri, username, password);
    }

    private static record Content(String uri, String username, String password) {
    }

    public enum TransactionIsolation {
        TRANSACTION_READ_COMMITTED("TRANSACTION_READ_COMMITTED"),
        TRANSACTION_SERIALIZABLE("TRANSACTION_SERIALIZABLE");

        private final String name;

        TransactionIsolation(String name) {
            this.name = name;
        }
    }
}
