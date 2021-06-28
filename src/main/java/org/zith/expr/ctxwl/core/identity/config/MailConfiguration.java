package org.zith.expr.ctxwl.core.identity.config;

import java.util.Objects;

public final class MailConfiguration {
    private final Content content;

    public MailConfiguration(String host, int port, boolean tls, String address, Authentication authentication) {
        this.content = makeContent(host, port, tls, address, authentication);
    }

    public String host() {
        return content.host();
    }

    public int port() {
        return content.port();
    }

    public boolean tls() {
        return content.tls();
    }

    public String address() {
        return content.address();
    }

    public Authentication authentication() {
        return content.authentication();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailConfiguration that = (MailConfiguration) o;
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return "MailConfiguration{" + content + '}';
    }

    private static Content makeContent(String host, int port, boolean tls, String address, Authentication authentication) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(address);
        Objects.requireNonNull(authentication);
        if (host.isEmpty()) throw new IllegalArgumentException();
        if (port <= 0 || port >= 0x10000) throw new IllegalArgumentException();
        if (address.isEmpty()) throw new IllegalArgumentException();
        return new Content(host, port, tls, address, authentication);
    }

    private record Content(String host, int port, boolean tls, String address, Authentication authentication) {
    }

    public interface Authentication {

        final class None extends AuthenticationBase<AuthenticationContent.None> implements Authentication {
            public None() {
                super(new AuthenticationContent.None());
            }
        }

        final class Plain extends AuthenticationBase<AuthenticationContent.Plain> implements Authentication {
            public Plain(String username, String password) {
                super(makeContext(username, password));
            }

            private static AuthenticationContent.Plain makeContext(String username, String password) {
                Objects.requireNonNull(username);
                Objects.requireNonNull(password);
                return new AuthenticationContent.Plain(username, password);
            }

            public String username() {
                return content.username();
            }

            public String password() {
                return content.password();
            }
        }
    }

    private static class AuthenticationBase<SpecializedContent extends AuthenticationContent> implements Authentication {
        protected final SpecializedContent content;

        protected AuthenticationBase(SpecializedContent content) {
            Objects.requireNonNull(content);
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthenticationBase<?> that = (AuthenticationBase<?>) o;
            return content.equals(that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public String toString() {
            return "Authentication{" + content + '}';
        }
    }

    private interface AuthenticationContent {
        record None() implements AuthenticationContent {
        }

        record Plain(String username, String password) implements AuthenticationContent {
        }
    }
}
