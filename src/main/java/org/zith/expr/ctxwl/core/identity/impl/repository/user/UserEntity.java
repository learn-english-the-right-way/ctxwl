package org.zith.expr.ctxwl.core.identity.impl.repository.user;

import com.google.common.base.Suppliers;
import jakarta.persistence.*;

import java.util.function.Supplier;

@Entity
public class UserEntity {
    private final Supplier<UserImpl> delegate = Suppliers.memoize(() -> new UserImpl(this));

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public UserImpl getDelegate() {
        return delegate.get();
    }
}
