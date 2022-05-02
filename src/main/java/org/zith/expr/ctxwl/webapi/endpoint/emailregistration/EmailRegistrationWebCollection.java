package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.webapi.access.ActiveResourceRole;
import org.zith.expr.ctxwl.webapi.access.Principal;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyPrincipal;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Path("/email_registration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmailRegistrationWebCollection {

    private final IdentityServiceSessionFactory identityServiceSessionFactory;
    private final SecurityContext securityContext;

    @Inject
    public EmailRegistrationWebCollection(
            IdentityServiceSessionFactory identityServiceSessionFactory,
            SecurityContext securityContext
    ) {
        this.identityServiceSessionFactory = identityServiceSessionFactory;
        this.securityContext = securityContext;
    }

    @POST
    public EmailRegistrationWebDocument create(EmailRegistrationWebDocument document) throws Exception {
        try (var session = identityServiceSessionFactory.openSession()) {
            record Execution(EmailRegistrationWebDocument result, Runnable delayedOperations) {
            }

            var execution = session.withTransaction(() -> {
                var emailRegistration =
                        session.emailRegistrationRepository().register(document.email(), document.password().get());
                Runnable sendMail = () ->
                        emailRegistration.getEmail().sendMessage(new Email.Message(
                                "Confirmation code",
                                "Code: " + emailRegistration.getConfirmationCode()));
                var authenticationKey =
                        emailRegistration.getControlledResource()
                                .getApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
                return new Execution(
                        new EmailRegistrationWebDocument(
                                emailRegistration.getEmail().getAddress(),
                                Optional.empty(),
                                authenticationKey,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()
                        ),
                        sendMail);
            });

            execution.delayedOperations().run();

            return execution.result();
        }
    }

    @Path("{address}")
    @PATCH
    @Authenticated
    public EmailRegistrationWebDocument update(
            @PathParam("address") String address,
            EmailRegistrationWebDocument document
    ) {
        try (var session = identityServiceSessionFactory.openSession()) {
            return session.withTransaction(() -> {
                var optionalEmailRegistration = session.emailRegistrationRepository().get(address);

                if (optionalEmailRegistration.isEmpty()) {
                    throw new UnauthorizedEmailAddressException();
                }

                var emailRegistration = optionalEmailRegistration.get();
                var registrationResource = emailRegistration.getControlledResource();
                var authorized =
                        CtxwlKeyPrincipal.resolveDelegate(securityContext.getUserPrincipal()).stream()
                                .map(Principal::roles)
                                .flatMap(Collection::stream)
                                .anyMatch(ActiveResourceRole.match(
                                        CredentialManager.ResourceType.EMAIL_REGISTRATION,
                                        registrationResource.getIdentifier()));

                if (!authorized) {
                    throw new UnauthorizedEmailAddressException();
                }

                if (!Objects.equals(
                        Optional.of(emailRegistration.getConfirmationCode()),
                        document.confirmationCode()
                )) {
                    throw new InvalidConfirmationCodeException();
                }

                var user = emailRegistration.getEmail().getUser().orElseGet(() -> {
                    var newUser = session.userRepository().register();
                    emailRegistration.getEmail().link(newUser);
                    newUser.getControlledResource().importKey(
                            registrationResource,
                            CredentialManager.KeyUsage.REGISTRATION_CREDENTIAL_PROPOSAL,
                            CredentialManager.KeyUsage.USER_LOGIN,
                            false);
                    return newUser;
                });
                var authenticationKey = user.getControlledResource().getApplicationKey(CredentialManager.KeyUsage.USER_AUTHENTICATION);

                var confirmationAuthenticationKey =
                        emailRegistration.getControlledResource()
                                .getApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
                return new EmailRegistrationWebDocument(
                        emailRegistration.getEmail().getAddress(),
                        Optional.empty(),
                        confirmationAuthenticationKey,
                        Optional.of(emailRegistration.getConfirmationCode()),
                        authenticationKey,
                        Optional.empty()
                );
            });
        }
    }
}
