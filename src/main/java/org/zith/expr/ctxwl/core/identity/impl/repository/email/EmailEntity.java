package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import com.google.common.base.Suppliers;
import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserEntity_;

import java.util.function.Supplier;

@Entity
public class EmailEntity {
    private final Supplier<EmailImpl> delegate = Suppliers.memoize(() -> new EmailImpl(this));

    private Long id;
    private String address;
    private UserEntity user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NaturalId
    @Column(columnDefinition = "text", unique = true)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @JoinColumn(name = "userId", referencedColumnName = UserEntity_.ID)
    @OneToOne
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity userEntity) {
        this.user = userEntity;
    }

    @Transient
    public EmailImpl getDelegate() {
        return delegate.get();
    }
}
