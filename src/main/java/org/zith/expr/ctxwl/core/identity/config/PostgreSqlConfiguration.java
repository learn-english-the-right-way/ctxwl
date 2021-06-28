package org.zith.expr.ctxwl.core.identity.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Objects;

public final class PostgreSqlConfiguration {
    private final Content content;

    public PostgreSqlConfiguration(
            String url,
            String username,
            String password
    ) {
        this.content = makeContent(url, username, password);
    }

    public String url() {
        return content.url();
    }

    public String username() {
        return content.username();
    }

    public String password() {
        return content.password();
    }

    public DataSource makeDataSource() {
        var config = new HikariConfig();
        config.setDataSourceClassName(PGSimpleDataSource.class.getName());
        config.addDataSourceProperty("url", url());
        config.setUsername(username());
        config.setPassword(password());
        return new HikariDataSource(config);
    }

    private static Content makeContent(String url, String username, String password) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        if (!url.startsWith("jdbc:postgresql:")) throw new IllegalArgumentException();
        if (username.isEmpty()) throw new IllegalArgumentException();
        if (password.isEmpty()) throw new IllegalArgumentException();
        return new Content(url, username, password);
    }

    private static record Content(String url, String username, String password) {
    }
}
