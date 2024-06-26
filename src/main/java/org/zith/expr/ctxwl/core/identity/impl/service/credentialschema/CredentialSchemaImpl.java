package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class CredentialSchemaImpl implements CredentialSchema {
    private final static Pattern PASSWORD_PATTERN = Pattern.compile("^[\\p{ASCII}&&[^\\p{Blank}\\p{Cntrl}]]{8,32}$");

    private final SecureRandom secureRandom;
    private final Clock clock;
    private MetaKeyChain metaKeyChain;

    private CredentialSchemaImpl(SecureRandom secureRandom, Clock clock) {
        Objects.requireNonNull(secureRandom);
        Objects.requireNonNull(clock);
        this.secureRandom = secureRandom;
        this.clock = clock;
        metaKeyChain = new MetaKeyChain(-1, new String[]{""});
    }

    public static CredentialSchema create(SecureRandom secureRandom, Clock clock) {
        return new CredentialSchemaImpl(secureRandom, clock);
    }

    @Override
    public byte[] makeSalt(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        secureRandom.nextBytes(salt);
        return salt;
    }

    @Override
    public Instant timestamp() {
        return clock.instant();
    }

    @Override
    public byte[] makeEntropicCode(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        secureRandom.nextBytes(salt);
        return salt;
    }

    @Override
    public void updateKeys(int offset, String[] keys) {
        metaKeyChain = new MetaKeyChain(0, keys);
    }

    @Override
    public boolean validateStructureOfPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public String makeName(ControlledResourceType resourceType, String identifier) {
        StringBuilder sb = new StringBuilder();
        sb.append(typeName(resourceType));
        sb.append(':');
        sb.append(identifier.replaceAll("[:\\\\]", "\\\\$0"));
        return sb.toString();
    }

    @Override
    public String typeName(ControlledResourceType resourceType) {
        return switch (resourceType) {
            case USER -> "user";
            case EMAIL_REGISTRATION -> "email_registration";
        };
    }

    @Override
    public String keyUsageName(CredentialManager.KeyUsage keyUsage) {
        return switch (keyUsage) {
            case REGISTRATION_CONFIRMATION -> "registration-confirmation";
            case REGISTRATION_CREDENTIAL_PROPOSAL -> "registration-credential-proposal";
            case USER_LOGIN -> "user-login";
            case USER_AUTHENTICATION -> "user-authentication";
        };
    }

    @Override
    public String makeApplicationKey(CredentialManager.KeyUsage keyUsage, byte[] code) {
        var keyType = metaKeyChain.current(keyUsage);

        var typeOffset = 4;
        var macOffset = typeOffset + code.length;
        var tailOffset = macOffset + 32;
        var buffer = new byte[tailOffset];
        System.arraycopy(Ints.toByteArray(keyType.code()), 0, buffer, 0, typeOffset);
        System.arraycopy(code, 0, buffer, typeOffset, code.length);
        keyType.hmac().hashBytes(buffer, 0, macOffset).writeBytesTo(buffer, macOffset, 32);
        return BaseEncoding.base64().encode(buffer);
    }

    @Override
    public Optional<byte[]> validateApplicationKey(Set<CredentialManager.KeyUsage> keyUsages, String applicationKey) {
        byte[] buffer;
        try {
            buffer = BaseEncoding.base64().decode(applicationKey);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        if (buffer.length < 36) {
            return Optional.empty();
        }

        var hmacs = metaKeyChain.resolveHmac(Ints.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3]), keyUsages);
        var accepted = hmacs.stream().anyMatch(hmac -> Arrays.equals(
                hmac.hashBytes(buffer, 0, buffer.length - 32).asBytes(), 0, 32,
                buffer, buffer.length - 32, buffer.length));
        if (accepted) {
            return Optional.of(Arrays.copyOfRange(buffer, 4, buffer.length - 32));
        } else {
            return Optional.empty();
        }
    }

    private final class MetaKeyChain {
        private final List<EnumMap<CredentialManager.KeyUsage, KeyType>> keyTypes;
        private final Map<Integer, List<KeyType>> typesByCode;

        MetaKeyChain(int offset, String[] metaKeys) {
            Preconditions.checkArgument((offset == -1 && Arrays.equals(metaKeys, new String[]{""})) || offset >= 0);
            this.keyTypes = IntStream.range(0, metaKeys.length)
                    .boxed()
                    .map(i -> Map.entry(offset + i, metaKeys[i]))
                    .map(e -> {
                        var serial = e.getKey();
                        var metaKey = e.getValue();
                        return new EnumMap<>(Arrays.stream(CredentialManager.KeyUsage.values())
                                .map(keyUsage -> {
                                    var metaKeyName = offset + ":" + metaKey;
                                    var hmac =
                                            Hashing.hmacSha256(metaKeyName.getBytes(StandardCharsets.UTF_8));
                                    var fullCode =
                                            hmac.hashBytes(keyUsageName(keyUsage).getBytes(StandardCharsets.UTF_8))
                                                    .asBytes();

                                    var code = Stream.iterate(0, i -> i < 8, i -> i + 4)
                                            .map(i -> Ints.fromBytes(
                                                    fullCode[i],
                                                    fullCode[i + 1],
                                                    fullCode[i + 2],
                                                    fullCode[i + 3]))
                                            .reduce(0, (a, b) -> a ^ b);
                                    return new KeyType(serial, keyUsage, hmac, code);
                                })
                                .collect(Collectors.toMap(KeyType::keyUsage, Function.identity())));
                    })
                    .toList();
            this.typesByCode =
                    keyTypes.stream()
                            .flatMap(v -> v.values().stream())
                            .collect(Collectors.groupingBy(KeyType::code));
        }

        KeyType current(CredentialManager.KeyUsage keyUsage) {
            return keyTypes.get(keyTypes.size() - 1).get(keyUsage);
        }

        List<HashFunction> resolveHmac(int code, Set<CredentialManager.KeyUsage> keyUsages) {
            return Stream.ofNullable(typesByCode.get(code))
                    .flatMap(Collection::stream)
                    .filter(t -> keyUsages.contains(t.keyUsage()))
                    .map(KeyType::hmac)
                    .toList();
        }

        static record KeyType(int serial, CredentialManager.KeyUsage keyUsage, HashFunction hmac, int code) {
        }
    }
}
