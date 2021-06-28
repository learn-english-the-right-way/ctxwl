package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Random;
import java.util.regex.Pattern;

public class CredentialRepositoryImpl implements CredentialRepository {
    private final static Pattern PASSWORD_PATTERN = Pattern.compile("^[\\p{ASCII}&&[^\\p{Blank}\\p{Cntrl}]]{8,32}$");

    private final Session session;
    private final Clock clock;
    private final Random random;

    public CredentialRepositoryImpl(Session session, Clock clock) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(clock);

        this.session = session;
        this.clock = clock;
        random = new Random();
    }

    @Override
    public ControlledResource ensure(ResourceType resourceType, String identifier) {
        var name = makeName(resourceType, identifier);
        return session
                .byNaturalId(ResourceEntity.class)
                .using("name", name)
                .with(LockOptions.READ)
                .loadOptional()
                .map(ResourceEntity::getDelegate)
                .map(r -> r.bind(this))
                .orElseGet(() -> ControlledResourceImpl.create(this, name));
    }

    @Override
    public boolean validatePassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private String makeName(ResourceType resourceType, String identifier) {
        StringBuilder sb = new StringBuilder();
        switch (resourceType) {
            case EMAIL_REGISTRATION -> sb.append("email_registration");
        }
        sb.append(':');
        sb.append(identifier.replaceAll("[:\\\\]", "\\\\$0"));
        return sb.toString();
    }

    Session getSession() {
        return session;
    }

    byte[] makeSalt(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        random.nextBytes(salt);
        return salt;
    }

    Instant timestamp() {
        return clock.instant();
    }

    byte[] makeEntropicCode(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        random.nextBytes(salt);
        return salt;
    }

    String makeAuthenticationKey(KeyUsage keyUsage, byte[] code) {
        var typeOffset = 4;
        var macOffset = typeOffset + code.length;
        var tailOffset = macOffset + 32;
        var buffer = new byte[tailOffset];
        Hashing.murmur3_32()
                .hashBytes(keyUsageName(keyUsage).getBytes(StandardCharsets.UTF_8))
                .writeBytesTo(buffer, 0, typeOffset);
        System.arraycopy(code, 0, buffer, typeOffset, code.length);
        Hashing.hmacSha256("TestKey".getBytes(StandardCharsets.UTF_8)/* TODO */).hashBytes(buffer, 0, macOffset)
                .writeBytesTo(buffer, macOffset, 32);
        return BaseEncoding.base64().encode(buffer);
    }

    static String keyUsageName(CredentialRepository.KeyUsage keyUsage) {
        switch (keyUsage) {
            case REGISTRATION:
                return "registration";
            case REGISTRATION_CONFIRMATION:
                return "registration-confirmation";
            case USER_LOGIN:
                return "user-login";
            case USER_AUTHENTICATION:
                return "user-authentication";
        }
        throw new IllegalStateException();
    }
}
