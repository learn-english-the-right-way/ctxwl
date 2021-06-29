package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
abstract class AbstractCredentialRepository {
    private final static Pattern PASSWORD_PATTERN = Pattern.compile("^[\\p{ASCII}&&[^\\p{Blank}\\p{Cntrl}]]{8,32}$");

    private MetaKeyChain metaKeyChain;

    protected AbstractCredentialRepository() {
        metaKeyChain = new MetaKeyChain(-1, new String[]{""});
    }

    void updateKeys(int offset, String[] keys) {
        metaKeyChain = new MetaKeyChain(0, keys);
    }

    boolean validatePassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    String makeName(CredentialRepository.ResourceType resourceType, String identifier) {
        StringBuilder sb = new StringBuilder();
        sb.append(switch (resourceType) {
            case EMAIL_REGISTRATION -> "email_registration";
        });
        sb.append(':');
        sb.append(identifier.replaceAll("[:\\\\]", "\\\\$0"));
        return sb.toString();
    }

    String makeAuthenticationKey(CredentialRepository.KeyUsage keyUsage, byte[] code) {
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

    boolean validateAuthenticationKey(CredentialRepository.KeyUsage keyUsage, String authenticationKey) {
        byte[] buffer = BaseEncoding.base64().decode(authenticationKey);
        var hmacs = metaKeyChain.resolveHmac(Ints.fromBytes(buffer[0], buffer[1], buffer[2], buffer[3]), keyUsage);
        return hmacs.stream().anyMatch(hmac -> Arrays.equals(
                hmac.hashBytes(buffer, 0, buffer.length - 32).asBytes(), 0, 32,
                buffer, buffer.length - 32, buffer.length));
    }

    static String keyUsageName(CredentialRepository.KeyUsage keyUsage) {
        return switch (keyUsage) {
            case REGISTRATION -> "registration";
            case REGISTRATION_CONFIRMATION -> "registration-confirmation";
            case USER_LOGIN -> "user-login";
            case USER_AUTHENTICATION -> "user-authentication";
        };
    }

    private static class MetaKeyChain {
        private final List<EnumMap<CredentialRepository.KeyUsage, KeyType>> keyTypes;
        private final Map<Integer, List<KeyType>> typesByCode;

        MetaKeyChain(int offset, String[] metaKeys) {
            Preconditions.checkArgument((offset == -1 && Arrays.equals(metaKeys, new String[]{""})) || offset >= 0);
            this.keyTypes = IntStream.range(0, metaKeys.length)
                    .boxed()
                    .map(i -> Map.entry(offset + i, metaKeys[i]))
                    .map(e -> {
                        var serial = e.getKey();
                        var metaKey = e.getValue();
                        return new EnumMap<>(Arrays.stream(CredentialRepository.KeyUsage.values())
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

        KeyType current(CredentialRepository.KeyUsage keyUsage) {
            return keyTypes.get(keyTypes.size() - 1).get(keyUsage);
        }

        List<HashFunction> resolveHmac(int code, CredentialRepository.KeyUsage keyUsage) {
            return Stream.ofNullable(typesByCode.get(code))
                    .flatMap(Collection::stream)
                    .filter(t -> Objects.equals(t.keyUsage(), keyUsage))
                    .map(KeyType::hmac)
                    .toList();
        }

        static record KeyType(int serial, CredentialRepository.KeyUsage keyUsage, HashFunction hmac, int code) {
        }
    }
}
