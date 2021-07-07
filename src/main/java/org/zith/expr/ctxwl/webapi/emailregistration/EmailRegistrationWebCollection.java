package org.zith.expr.ctxwl.webapi.emailregistration;

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
            var optionalEmailRegistration = session.emailRegistrationRepository().get(address);

            if (optionalEmailRegistration.isEmpty()) {
                throw new NotFoundException();
            }

            optionalEmailRegistration.ifPresent(emailRegistration -> {
                var authorized = principals.stream()
                        .filter(p -> p.getType() == CredentialManager.ResourceType.EMAIL_REGISTRATION)
                        .map(SimplePrincipal::getIdentifier)
                        .anyMatch(emailRegistration.getControlledResource().getIdentifier()::equals);

                if (!authorized) {
                    throw new ForbiddenException();
                }

                if (!Objects.equals(
                        Optional.of(emailRegistration.getConfirmationCode()),
                        document.confirmationCode()
                )) {
                    throw new ForbiddenException();
                }
            });
        }
        return document;
    }
}
