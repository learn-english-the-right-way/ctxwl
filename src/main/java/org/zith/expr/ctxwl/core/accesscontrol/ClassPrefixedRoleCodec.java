package org.zith.expr.ctxwl.core.accesscontrol;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassPrefixedRoleCodec implements RoleCodec {
    private final ArrayList<RoleClassPrefix<? extends Role>> prefixes;
    private final Map<String, Function<String, Role>> decoderMap;

    private ClassPrefixedRoleCodec(
            ArrayList<RoleClassPrefix<? extends Role>> prefixes,
            Map<String, Function<String, Role>> decoderMap
    ) {
        this.prefixes = prefixes;
        this.decoderMap = decoderMap;
    }

    @Override
    public Optional<String> encode(Role role) {
        return prefixes.stream()
                .map(p -> p.encode(role))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Role> decode(String role) {
        var pos = role.indexOf(':');
        if (pos == -1) return Optional.empty();
        return Optional.ofNullable(decoderMap.get(role.substring(0, pos))).map(p -> p.apply(role.substring(pos + 1)));
    }

    @NotNull
    public static RoleCodec of(List<RoleClassPrefix<? extends Role>> prefixes) {
        Objects.requireNonNull(prefixes);
        var prefixPattern = Pattern.compile("[\\p{Alnum}_\\-]+");
        prefixes.forEach((e) -> {
            Objects.requireNonNull(e.clazz());
            Objects.requireNonNull(e.prefix());
            Objects.requireNonNull(e.encoder());
            Objects.requireNonNull(e.decoder());
            Preconditions.checkArgument(
                    prefixPattern.matcher(e.prefix()).matches(),
                    "Prefix '%s' is illegal", e.prefix());
        });

        var mappingsByPrefix = prefixes.stream().collect(Collectors.groupingBy(RoleClassPrefix::prefix));
        mappingsByPrefix.forEach((prefix, m) ->
                Preconditions.checkArgument(m.size() <= 1, "Prefix '%s' is duplicated", prefix));

        var prefixList = new ArrayList<>(prefixes);
        var decoderMap =
                prefixes.stream().collect(Collectors.toMap(RoleClassPrefix::prefix, RoleClassPrefix::generalizedDecoder));
        return new ClassPrefixedRoleCodec(prefixList, decoderMap);
    }

    @NotNull
    public static <R extends Role> RoleClassPrefix<R> prefix(
            Class<R> clazz,
            String prefix,
            Function<R, String> encoder,
            Function<String, R> decoder
    ) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(encoder);
        Objects.requireNonNull(decoder);
        return new RoleClassPrefix<>(clazz, prefix, encoder, decoder);
    }

    public record RoleClassPrefix<R extends Role>(
            Class<R> clazz,
            String prefix,
            Function<R, String> encoder,
            Function<String, R> decoder
    ) {
        Optional<String> encode(Role role) {
            return Optional.of(role).filter(clazz::isInstance).map(clazz::cast).map(encoder);
        }

        @SuppressWarnings("unchecked")
        Function<String, Role> generalizedDecoder() {
            return (Function<String, Role>) decoder();
        }
    }
}
