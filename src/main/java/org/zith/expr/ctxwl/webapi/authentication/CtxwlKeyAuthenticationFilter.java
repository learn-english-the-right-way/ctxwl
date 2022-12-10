package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Splitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

@Authenticated
public class CtxwlKeyAuthenticationFilter implements ContainerRequestFilter {
    private static final Splitter HEADER_SPLITTER = Splitter.on('.').trimResults();
    private final Realm realm;

    @Inject
    public CtxwlKeyAuthenticationFilter(Realm realm) {
        Objects.requireNonNull(realm);
        this.realm = realm;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var optionalPrincipal = realm.authenticate(
                Stream.ofNullable(requestContext.getHeaderString("X-Ctxwl-Key"))
                        .map(HEADER_SPLITTER::splitToList)
                        .flatMap(Collection::stream)
                        .toList());

        if (optionalPrincipal.isEmpty()) {
            throw new CtxwlKeyAuthenticationException();
        } else {
            requestContext.setSecurityContext(CtxwlKeySecurityContext.create(
                    requestContext.getSecurityContext(),
                    optionalPrincipal.get()));
        }
    }
}
