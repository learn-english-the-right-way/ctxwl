package org.zith.expr.ctxwl.webapi.authentication;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.Factory;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;

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
                        ControlledResourceType.EMAIL_REGISTRATION,
                        CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION)
                .authenticationMethod(
                        ControlledResourceType.USER,
                        CredentialManager.KeyUsage.USER_AUTHENTICATION)
                .credentialManager(identityService.credentialManager())
                .build();
    }

    @Override
    public void dispose(Realm instance) {

    }
}
