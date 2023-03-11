package org.zith.expr.ctxwl.core.identity.functest;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.webapi.authorization.role.EmailRegistrantRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        var principals = realm().authenticate(List.of(authenticationKey));
        assertFalse(principals.isEmpty());
        var principal = principals.get(0);

        try (var session = identityService().openSession()) {
            var emailRegistrations =
                    session.emailRegistrationRepository().list(address);
            assertEquals(1, emailRegistrations.size());
            var emailRegistration = emailRegistrations.get(0);
            accessPolicy().isPrincipalInRole(principal, new EmailRegistrantRole(emailRegistration.getControlledResource().getIdentifier()));
        }
    }
}
