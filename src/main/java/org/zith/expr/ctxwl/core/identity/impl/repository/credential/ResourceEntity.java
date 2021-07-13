package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Suppliers;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Collection;
import java.util.function.Supplier;

@Entity
public class ResourceEntity {
    private final Supplier<ControlledResourceImpl> delegate = Suppliers.memoize(() -> new ControlledResourceImpl(this));

    private Long id;
    private String name;
    private String type;
    private String identifier;
    private Integer entrySerial;
    private Collection<ResourcePasswordEntity> passwords;
    private Collection<ResourceAuthenticationKeyEntity> authenticationKeys;

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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Column(nullable = false)
    public Integer getEntrySerial() {
        return entrySerial;
    }

    public void setEntrySerial(Integer entrySerial) {
        this.entrySerial = entrySerial;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resourceId", referencedColumnName = "id")
    @Where(clause = "invalidation is null")
    public Collection<ResourcePasswordEntity> getPasswords() {
        return passwords;
    }

    public void setPasswords(Collection<ResourcePasswordEntity> passwords) {
        this.passwords = passwords;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resourceId", referencedColumnName = "id")
    @Where(clause = "invalidation is null")
    public Collection<ResourceAuthenticationKeyEntity> getAuthenticationKeys() {
        return authenticationKeys;
    }

    public void setAuthenticationKeys(Collection<ResourceAuthenticationKeyEntity> authenticationKeys) {
        this.authenticationKeys = authenticationKeys;
    }

    @Transient
    public ControlledResourceImpl getDelegate() {
        return delegate.get();
    }
}
