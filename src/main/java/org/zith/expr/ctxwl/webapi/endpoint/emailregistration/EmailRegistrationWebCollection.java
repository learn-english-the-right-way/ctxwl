package org.zith.expr.ctxwl.webapi.endpoint.emailregistration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.webapi.authentication.Authenticated;
import org.zith.expr.ctxwl.webapi.authentication.Authentication;
import org.zith.expr.ctxwl.webapi.authentication.SimplePrincipal;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
                                .getAuthenticationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
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
    public EmailRegistrationWebDocument update(@PathParam("address") String address, EmailRegistrationWebDocument document) {
        var principals = Authentication.principals(securityContext).stream()
                .flatMap(principal -> {
                    if (principal instanceof SimplePrincipal simplePrincipal) {
                        return Stream.of(simplePrincipal);
                    } else {
                        return Stream.empty();
                    }
                })
                .toList();
        try (var session = identityServiceSessionFactory.openSession()) {
            return session.withTransaction(() -> {
                var optionalEmailRegistration = session.emailRegistrationRepository().get(address);

                if (optionalEmailRegistration.isEmpty()) {
                    throw new NotFoundException();
                }

                var emailRegistration = optionalEmailRegistration.get();
                var registrationResource = emailRegistration.getControlledResource();
                var authorized = principals.stream()
                        .filter(p -> p.getType() == CredentialManager.ResourceType.EMAIL_REGISTRATION)
                        .map(SimplePrincipal::getIdentifier)
                        .anyMatch(registrationResource.getIdentifier()::equals);

                if (!authorized) {
                    throw new ForbiddenException();
                }

                if (!Objects.equals(
                        Optional.of(emailRegistration.getConfirmationCode()),
                        document.confirmationCode()
                )) {
                    throw new ForbiddenException();
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
                var authenticationKey = user.getControlledResource().getAuthenticationKey(CredentialManager.KeyUsage.USER_AUTHENTICATION);

                var confirmationAuthenticationKey =
                        emailRegistration.getControlledResource()
                                .getAuthenticationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
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
