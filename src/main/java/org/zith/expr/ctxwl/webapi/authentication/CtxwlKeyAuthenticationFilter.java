package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Splitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import org.zith.expr.ctxwl.core.accesscontrol.AccessPolicy;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

@Authenticated
public class CtxwlKeyAuthenticationFilter implements ContainerRequestFilter {
    private static final Splitter HEADER_SPLITTER = Splitter.on('.').trimResults();
    private final Realm realm;
    private final AccessPolicy policy;

    @Inject
    public CtxwlKeyAuthenticationFilter(Realm realm, AccessPolicy policy) {
        Objects.requireNonNull(realm);
        this.realm = realm;
        this.policy = policy;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var principals = realm.authenticate(
                Stream.ofNullable(requestContext.getHeaderString("X-Ctxwl-Key"))
                        .map(HEADER_SPLITTER::splitToList)
                        .flatMap(Collection::stream)
                        .toList());

        if (principals.isEmpty()) {
            throw new CtxwlKeyAuthenticationException();
        } else {
            requestContext.setSecurityContext(
                    CtxwlSecurityContext.create(requestContext.getSecurityContext(), policy, principals));
        }
    }
}
