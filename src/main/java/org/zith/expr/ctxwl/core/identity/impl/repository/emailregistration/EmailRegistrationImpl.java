package org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration;

import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;
import org.zith.expr.ctxwl.core.identity.Email;
import org.zith.expr.ctxwl.core.identity.EmailRegistration;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailImpl;

import java.time.Instant;

public class EmailRegistrationImpl implements EmailRegistration {

    private final EmailRegistrationEntity entity;
    private EmailRegistrationRepositoryImpl repository;
    private Email email;
    private ControlledResource controlledResource;

    public EmailRegistrationImpl(EmailRegistrationEntity entity) {
        this.entity = entity;
    }

    public EmailRegistrationEntity getEntity() {
        return entity;
    }

    public Long getId() {
        return entity.getId();
    }

    @Override
    public String getConfirmationCode() {
        return entity.getConfirmationCode();
    }

    @Override
    public Email getEmail() {
        if (email == null) {
            email = repository.getEmailRepository().ensure(entity.getEmail().getAddress());
        }
        return email;
    }

    @Override
    public ControlledResource getControlledResource() {
        if (controlledResource == null) {
            controlledResource = repository.getCredentialRepository().ensure(
                    CredentialManager.ResourceType.EMAIL_REGISTRATION,
                    entity.getId().toString()
            );
        }
        return controlledResource;
    }

    public EmailRegistrationImpl bind(EmailRegistrationRepositoryImpl repository) {
        this.repository = repository;
        return this;
    }

    private void initialize(EmailImpl email, String password, String confirmationCode, Instant initiation) {
        this.email = email;

        entity.setEmail(email.getEntity());
        entity.setConfirmationCode(confirmationCode);
        entity.setInitiation(initiation);
        repository.getSession().persist(entity);

        var controlledResource = getControlledResource();
        controlledResource.setPassword(CredentialManager.KeyUsage.REGISTRATION_CREDENTIAL_PROPOSAL, password);
        controlledResource.ensureApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION);
    }


    public static EmailRegistrationImpl create(
            EmailRegistrationRepositoryImpl repository,
            EmailImpl email,
            String password,
            String confirmationCode,
            Instant initiation
    ) {
        var emailRegistration = new EmailRegistrationEntity().getDelegate().bind(repository);
        emailRegistration.initialize(email, password, confirmationCode, initiation);
        return emailRegistration;
    }
}
