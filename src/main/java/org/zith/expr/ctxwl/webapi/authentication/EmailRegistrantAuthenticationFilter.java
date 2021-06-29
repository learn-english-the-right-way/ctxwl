package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.inject.Inject;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;

@LimitedToEmailRegistrant
public class EmailRegistrantAuthenticationFilter extends AbstractAuthenticationFilter {
    @Inject
    public EmailRegistrantAuthenticationFilter(IdentityService identityService) {
        super(identityService.credentialManager(), CredentialManager.PrincipalType.EMAIL_REGISTRANT);
    }
}
