package org.zith.expr.ctxwl.core.identity.impl.service.mail;

import org.jetbrains.annotations.Nullable;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.EmailException;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Properties;

public class MailServiceImpl implements MailService {

    private final String host;
    private final int port;
    private final boolean tls;
    private final String address;
    private final @Nullable Authenticator authenticator;

    private MailServiceImpl(String host, int port, boolean tls, String address, @Nullable Authenticator authenticator) {
        this.host = host;
        this.port = port;
        this.tls = tls;
        this.address = address;
        this.authenticator = authenticator;
    }

    private Session makeSession() {
        var props = new Properties();

        props.put("mail.smtp.auth", authenticator != null);
        props.put("mail.smtp.starttls.enable", tls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        return Session.getInstance(props, authenticator);
    }

    @Override
    public void sendMessage(String to, Email.Message message) {
        Objects.requireNonNull(message);
        var session = makeSession();
        var mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(this.address);
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(message.subject());
            mimeMessage.setText(message.text());
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }

    private static class SimpleAuthenticator extends Authenticator {
        private final String username;
        private final String password;

        private SimpleAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    public static MailServiceImpl create(MailConfiguration configuration) {
        var host = configuration.host();
        var port = configuration.port();
        var tls = configuration.tls();
        var address = configuration.address();

        Authenticator authenticator;
        if (configuration.authentication() instanceof MailConfiguration.Authentication.None) {
            authenticator = null;
        } else if (configuration.authentication() instanceof MailConfiguration.Authentication.Plain plain) {
            authenticator = new SimpleAuthenticator(plain.username(), plain.password());
        } else {
            throw new IllegalArgumentException();
        }

        return new MailServiceImpl(host, port, tls, address, authenticator);
    }
}
