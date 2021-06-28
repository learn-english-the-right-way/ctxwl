package org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration;

import com.google.common.base.Suppliers;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailEntity;

import javax.persistence.*;
import java.time.Instant;
import java.util.function.Supplier;

@Entity
public class EmailRegistrationEntity {
    private final Supplier<EmailRegistrationImpl> delegate = Suppliers.memoize(() -> new EmailRegistrationImpl(this));

    private Long id;
    private EmailEntity email;
    private String confirmationCode;
    private Instant initiation;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JoinColumn(name = "emailId", referencedColumnName = "id")
    @ManyToOne
    public EmailEntity getEmail() {
        return email;
    }

    public void setEmail(EmailEntity email) {
        this.email = email;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getInitiation() {
        return initiation;
    }

    public void setInitiation(Instant initiation) {
        this.initiation = initiation;
    }

    @Transient
    public EmailRegistrationImpl getDelegate() {
        return delegate.get();
    }
}
