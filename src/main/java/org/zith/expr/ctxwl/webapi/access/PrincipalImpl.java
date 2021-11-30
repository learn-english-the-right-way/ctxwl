package org.zith.expr.ctxwl.webapi.access;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

class PrincipalImpl implements Principal {
    private final Realm realm;
    private final List<VerifiedCredential> verifiedCredentials;
    private final Supplier<List<Role>> rolesSupplier;

    private PrincipalImpl(Realm realm, List<VerifiedCredential> verifiedCredentials) {
        this.realm = realm;
        this.verifiedCredentials = Objects.requireNonNull(verifiedCredentials);
        rolesSupplier =
                Suppliers.memoize(() -> Stream.<Stream<Role>>of(
                                this.verifiedCredentials.stream().map(VerifiedCredential::activeResourceRole),
                                this.verifiedCredentials.stream().map(VerifiedCredential::applicationKeyRole))
                        .flatMap(Function.identity())
                        .toList());
    }

    @NotNull
    static PrincipalImpl create(RealmImpl realm, List<VerifiedCredential> c) {
        Preconditions.checkNotNull(realm);
        Preconditions.checkArgument(!c.isEmpty());
        return new PrincipalImpl(realm, c);
    }

    @Override
    public Realm realm() {
        return realm;
    }

    @Override
    public List<Role> roles() {
        return rolesSupplier.get();
    }

    @Override
    public Subject subject() {
        return verifiedCredentials.get(0).subject();
    }
}
