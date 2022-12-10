package org.zith.expr.ctxwl.webapi.endpoint.authentication;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.EmailRegistration;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;

import java.util.List;
import java.util.stream.Collectors;

@Path("/authentication")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationWebCollection {
    private static final String AUTHENTICATION_METHOD_PREFIX_USER_EMAIL = "user.email:";
    private static final String AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL = "registration.email:";

    private final IdentityService identityService;
    private final Realm realm;

    @Inject
    public AuthenticationWebCollection(
            IdentityService identityService,
            Realm realm
    ) {
        this.identityService = identityService;
        this.realm = realm;
    }

    @POST
    @Path("/{authenticationMethod}/application_key_challenge")
    public ApplicationKeyChallengeResultWebDocument applicationKeyChallenge(
            @PathParam("authenticationMethod") String authenticationMethod,
            ApplicationKeyChallengePostWebDocument document
    ) throws Exception {
        try (var session = identityService.openSession()) {
            return session.withTransaction(() -> {
                List<ControlledResource> controlledResources;
                CredentialManager.KeyUsage loginKeyUsage;
                if (authenticationMethod.startsWith(AUTHENTICATION_METHOD_PREFIX_USER_EMAIL)) {
                    var key = authenticationMethod.substring(AUTHENTICATION_METHOD_PREFIX_USER_EMAIL.length());
                    var optionalEmail = session.emailRepository().get(key);
                    if (optionalEmail.isEmpty()) {
                        throw new AuthenticationException.InvalidCredentialException();
                    }
                    var email = optionalEmail.get();
                    var optionalUser = email.getUser();
                    if (optionalUser.isEmpty()) {
                        throw new AuthenticationException.InvalidCredentialException();
                    }
                    var user = optionalUser.get();
                    controlledResources = List.of(user.getControlledResource());
                    loginKeyUsage = CredentialManager.KeyUsage.USER_LOGIN;
                } else if (authenticationMethod.startsWith(AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL)) {
                    var key = authenticationMethod.substring(AUTHENTICATION_METHOD_PREFIX_REGISTRATION_EMAIL.length());
                    var emailRegistrations = session.emailRegistrationRepository().list(key);
                    if (emailRegistrations.isEmpty()) {
                        throw new AuthenticationException.InvalidCredentialException();
                    }
                    controlledResources =
                            emailRegistrations.stream()
                                    .map(EmailRegistration::getControlledResource)
                                    .collect(Collectors.toList());
                    loginKeyUsage = CredentialManager.KeyUsage.REGISTRATION_CREDENTIAL_PROPOSAL;
                } else {
                    throw new AuthenticationException.UnsupportedAuthenticationMethodException(authenticationMethod.replaceAll(":.*$", ""));
                }

                var authenticatedControlledResource =
                        controlledResources.stream()
                                .filter(r -> r.validatePassword(loginKeyUsage, document.password()))
                                .findFirst();

                if (authenticatedControlledResource.isEmpty()) {
                    throw new AuthenticationException.InvalidCredentialException();
                }

                var controlledResource = authenticatedControlledResource.get();

                var optionalAuthenticatingKeyUsage =
                        realm.resolveAuthenticatingKeyUsage(controlledResource.getType());
                if (optionalAuthenticatingKeyUsage.isEmpty()) {
                    throw new AuthenticationException.InvalidCredentialException();
                }

                var authenticatingKeyUsage = optionalAuthenticatingKeyUsage.get();
                var authenticationKey = controlledResource.ensureApplicationKey(authenticatingKeyUsage);

                return new ApplicationKeyChallengeResultWebDocument(authenticationKey);
            });
        }
    }

}
