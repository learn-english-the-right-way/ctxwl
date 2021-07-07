package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import static org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceAuthenticationKeyEntity_.CODE_ID;
import static org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceAuthenticationKeyEntity_.RESOURCE_ID;

@Entity
@IdClass(ResourceAuthenticationKeyEntity.Key.class)
public class ResourceAuthenticationKeyEntity {
    private Long resourceId;
    private Integer id;
    private ResourceEntity resource;
    private String keyUsage;
    private byte[] code;
    private Long codeId;
    private ResourceAuthenticationKeyCodeEntity effectiveCode;
    private Instant creation;
    private Instant expiry;
    private Instant invalidation;

    @Id
    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JoinColumn(name = RESOURCE_ID, referencedColumnName = ResourceEntity_.ID, updatable = false, insertable = false)
    @ManyToOne
    public ResourceEntity getResource() {
        return resource;
    }

    public void setResource(ResourceEntity resource) {
        this.resource = resource;
    }

    @Column(columnDefinition = "text")
    public String getKeyUsage() {
        return keyUsage;
    }

    public void setKeyUsage(String keyUsage) {
        this.keyUsage = keyUsage;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    @JoinColumn(
            name = CODE_ID,
            referencedColumnName = ResourceAuthenticationKeyCodeEntity_.ID,
            insertable = false,
            updatable = false
    )
    @OneToOne(fetch = FetchType.LAZY)
    public ResourceAuthenticationKeyCodeEntity getEffectiveCode() {
        return effectiveCode;
    }

    public void setEffectiveCode(ResourceAuthenticationKeyCodeEntity effectiveCode) {
        this.effectiveCode = effectiveCode;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getCreation() {
        return creation;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getExpiry() {
        return expiry;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getInvalidation() {
        return invalidation;
    }

    public void setInvalidation(Instant invalidation) {
        this.invalidation = invalidation;
    }

    public static class Key implements Serializable {
        private Long resourceId;
        private Integer id;

        public Key(Long resourceId, Integer id) {
            this.resourceId = resourceId;
            this.id = id;
        }

        public Key() {
        }

        @Id
        public Long getResourceId() {
            return resourceId;
        }

        public void setResourceId(Long resourceId) {
            this.resourceId = resourceId;
        }

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(resourceId, key.resourceId) && Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceId, id);
        }
    }
}
