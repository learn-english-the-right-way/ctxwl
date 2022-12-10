package org.zith.expr.ctxwl.core.identity.functest.config;

import org.zith.expr.ctxwl.app.config.Configurations;
import org.zith.expr.ctxwl.common.configuration.Configuration;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;

public record IdentityServiceTestMailConfiguration(
        String host,
        Integer port,
        Boolean tls,
        String address,
        Method method,
        PlainAuthenticationConfiguration plainAuthentication
) implements Configuration<IdentityServiceTestMailConfiguration> {

    @Override
    public IdentityServiceTestMailConfiguration merge(IdentityServiceTestMailConfiguration overriding) {
        return new IdentityServiceTestMailConfiguration(
                Configurations.overlay(host(), overriding.host()),
                Configurations.overlay(port(), overriding.port()),
                Configurations.overlay(tls(), overriding.tls()),
                Configurations.overlay(address(), overriding.address()),
                Configurations.overlay(method(), overriding.method()),
                Configurations.merge(plainAuthentication(), overriding.plainAuthentication())
        );
    }

    public MailConfiguration effectiveConfiguration() {
        var host = host();
        var port = port();
        var tls = tls();
        if (tls == null) {
            tls = true;
        }
        if (port == null) {
            if (tls) {
                port = 465;
            } else {
                port = 587;
            }
        }
        var address = address();
        MailConfiguration.Authentication authentication;
        if (method() == null || method() == Method.none) {
            authentication = new MailConfiguration.Authentication.None();
        } else if (method() == Method.plain) {
            authentication = new MailConfiguration.Authentication.Plain(
                    plainAuthentication().username(),
                    plainAuthentication().password()
            );
        } else {
            throw new IllegalArgumentException();
        }
        return new MailConfiguration(host, port, tls, address, authentication);
    }

    public static IdentityServiceTestMailConfiguration empty() {
        return new IdentityServiceTestMailConfiguration(null, null, null, null, null, null);
    }

    public enum Method {
        none,
        plain,
    }

    public static record PlainAuthenticationConfiguration(String username, String password)
            implements Configuration<PlainAuthenticationConfiguration> {
        @Override
        public PlainAuthenticationConfiguration merge(PlainAuthenticationConfiguration overriding) {
            return new PlainAuthenticationConfiguration(
                    Configurations.overlay(username(), overriding.username()),
                    Configurations.overlay(password(), overriding.password())
            );
        }
    }

}
