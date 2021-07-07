package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@IdClass(ResourcePasswordEntity.Key.class)
public class ResourcePasswordEntity {
    private Long resourceId;
    private Integer id;
    private ResourceEntity resource;
    private String keyUsage;
    private String algorithm;
    private String salt;
    private String hashedPassword;
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

    @JoinColumn(name = "resourceId", referencedColumnName = "id", updatable = false, insertable = false)
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

    @Column(columnDefinition = "text")
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Column(columnDefinition = "text")
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Column(columnDefinition = "text")
    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
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
            ResourcePasswordEntity.Key key = (ResourcePasswordEntity.Key) o;
            return Objects.equals(resourceId, key.resourceId) && Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceId, id);
        }
    }
}
