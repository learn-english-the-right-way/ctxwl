package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.Factory;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;

public class RealmFactory implements Factory<Realm> {
    private final IdentityService identityService;

    @Inject
    public RealmFactory(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public Realm provide() {
        return Realm.builder()
                .authenticationMethod(
                        CredentialManager.ResourceType.EMAIL_REGISTRATION,
                        CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION)
                .authenticationMethod(
                        CredentialManager.ResourceType.USER,
                        CredentialManager.KeyUsage.USER_AUTHENTICATION)
                .credentialManager(identityService.credentialManager())
                .build();
    }

    @Override
    public void dispose(Realm instance) {

    }
}
