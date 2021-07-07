package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;

import java.util.List;

@Authenticated
public class DefaultAuthenticationFilter extends AbstractAuthenticationFilter {
    @Inject
    public DefaultAuthenticationFilter(IdentityService identityService) {
        super(identityService.credentialManager(), CredentialManager.Domain.GENERAL_ACCESS);
    }

    @Override
    protected SecurityContext makeSecurityContext(SecurityContext securityContext, List<ControlledResource> principals) {
        return DefaultSecurityContext.create(securityContext, domain(), principals);
    }
}
