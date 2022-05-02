package org.zith.expr.ctxwl.core.identity.inttest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.webapi.accesscontrol.ActiveResourceRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailRegistrationRepositoryIntegrationTests extends AbstractIdentityServiceIntegrationTests {
    @Test
    public void testRegistration() {
        String authenticationKey;
        var address = "test@example.com";
        try (var session = identityService().openSession()) {
            var optionalAuthenticationKey = session.withTransaction(() -> {
                var emailRegistration =
                        session.emailRegistrationRepository()
                                .register(address, "TESTpassword");
                return emailRegistration.getControlledResource()
                        .getApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
            });
            assertTrue(optionalAuthenticationKey.isPresent());
            authenticationKey = optionalAuthenticationKey.get();
        }
        var optionalPrincipal = realm().authenticate(List.of(authenticationKey));
        assertTrue(optionalPrincipal.isPresent());
        var principal = optionalPrincipal.get();
        assertTrue(principal.roles().stream()
                .anyMatch(ActiveResourceRole.match(CredentialManager.ResourceType.EMAIL_REGISTRATION)));

        try (var session = identityService().openSession()) {
            var optionalEmailRegistration =
                    session.emailRegistrationRepository().get(address);
            assertTrue(optionalEmailRegistration.isPresent());
            var emailRegistration = optionalEmailRegistration.get();
            assertTrue(principal.roles().stream()
                    .anyMatch(ActiveResourceRole.match(CredentialManager.ResourceType.EMAIL_REGISTRATION)));
        }
    }
}
