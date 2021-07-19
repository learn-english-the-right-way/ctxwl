package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.IdentityService;

@Path("/authentication")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationWebCollection {
    private static final String AUTHENTICATION_METHOD_PREFIX_USER_EMAIL = "user.email:";
    private static final String AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL = "registration.email:";

    private final IdentityService identityService;
    private final SecurityContext securityContext;

    @Inject
    public AuthenticationWebCollection(
            IdentityService identityService,
            SecurityContext securityContext
    ) {
        this.identityService = identityService;
        this.securityContext = securityContext;
    }

    @POST
    @Path("/{authenticationMethod}/application_key_challenge")
    public ApplicationKeyChallengeResultWebDocument applicationKeyChallenge(
            @PathParam("authenticationMethod") String authenticationMethod,
            ApplicationKeyChallengePostWebDocument document
    ) throws Exception {
        try (var session = identityService.openSession()) {
            return session.withTransaction(() -> {
                ControlledResource controlledResource;
                CredentialManager.KeyUsage loginKeyUsage;
                if (authenticationMethod.startsWith(AUTHENTICATION_METHOD_PREFIX_USER_EMAIL)) {
                    var key = authenticationMethod.substring(AUTHENTICATION_METHOD_PREFIX_USER_EMAIL.length());
                    var optionalEmail = session.emailRepository().get(key);
                    if (optionalEmail.isEmpty()) {
                        throw new ForbiddenException();
                    }
                    var email = optionalEmail.get();
                    var optionalUser = email.getUser();
                    if (optionalUser.isEmpty()) {
                        throw new ForbiddenException();
                    }
                    var user = optionalUser.get();
                    controlledResource = user.getControlledResource();
                    loginKeyUsage = CredentialManager.KeyUsage.USER_LOGIN;
                } else if (authenticationMethod.startsWith(AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL)) {
                    var key = authenticationMethod.substring(AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL.length());
                    var optionalEmailRegistration = session.emailRegistrationRepository().get(key);
                    if (optionalEmailRegistration.isEmpty()) {
                        throw new ForbiddenException();
                    }
                    var emailRegistration = optionalEmailRegistration.get();
                    controlledResource = emailRegistration.getControlledResource();
                    loginKeyUsage = CredentialManager.KeyUsage.REGISTRATION_CREDENTIAL_PROPOSAL;
                } else {
                    throw new ForbiddenException();
                }

                if (!controlledResource.validatePassword(loginKeyUsage, document.password())) {
                    throw new ForbiddenException();
                }

                var optionalAuthenticatingKeyUsage =
                        identityService.credentialManager()
                                .resolveAuthenticatingKeyUsage(
                                        CredentialManager.Domain.GENERAL_ACCESS,
                                        controlledResource.getType()
                                );
                if (optionalAuthenticatingKeyUsage.isEmpty()) {
                    throw new ForbiddenException();
                }

                var authenticatingKeyUsage = optionalAuthenticatingKeyUsage.get();
                var authenticationKey = controlledResource.ensureApplicationKey(authenticatingKeyUsage);

                return new ApplicationKeyChallengeResultWebDocument(authenticationKey);
            });
        }
    }

}