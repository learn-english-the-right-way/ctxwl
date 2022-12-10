package org.zith.expr.ctxwl.core.identity.functest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.webapi.accesscontrol.ActiveResourceRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailRegistrationRepositoryFunctionalTests extends AbstractIdentityServiceFunctionalTests {
    @Test
    public void testRegistration() {
        String authenticationKey;
        var address = "test@example.com";
        try (var session = identityService().openSession()) {
            session.withTransaction(() -> {
                session.emailRegistrationRepository()
                        .register(address, "TESTpassword");

                return null;
            });
        }
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
            var emailRegistrations =
                    session.emailRegistrationRepository().list(address);
            assertEquals(1, emailRegistrations.size());
            var emailRegistration = emailRegistrations.get(0);
            assertTrue(principal.roles().stream()
                    .anyMatch(ActiveResourceRole.match(CredentialManager.ResourceType.EMAIL_REGISTRATION)));
        }
    }
}
