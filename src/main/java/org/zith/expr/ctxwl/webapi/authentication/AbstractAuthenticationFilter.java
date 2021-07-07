package org.zith.expr.ctxwl.webapi.authentication;

import com.google.common.base.Splitter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@PreMatching
abstract class AbstractAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthenticationFilter.class);
    private static final Splitter HEADER_SPLITTER = Splitter.on('.').trimResults();
    private final CredentialManager credentialManager;
    private final CredentialManager.Domain domain;

    protected AbstractAuthenticationFilter(
            CredentialManager credentialManager,
            CredentialManager.Domain domain
    ) {
        Objects.requireNonNull(credentialManager);
        Objects.requireNonNull(domain);
        this.credentialManager = credentialManager;
        this.domain = domain;
    }

    protected final CredentialManager.Domain domain() {
        return domain;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var principals =
                Stream.ofNullable(requestContext.getHeaderString("X-Ctxwl-Key"))
                        .map(HEADER_SPLITTER::splitToList)
                        .flatMap(Collection::stream)
                        .flatMap(key -> credentialManager.authenticate(domain, key).stream())
                        .toList();

        if (principals.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        } else {
            requestContext.setSecurityContext(makeSecurityContext(requestContext.getSecurityContext(), principals));
        }
    }

    protected abstract SecurityContext makeSecurityContext(
            SecurityContext securityContext,
            List<ControlledResource> principals
    );
}
