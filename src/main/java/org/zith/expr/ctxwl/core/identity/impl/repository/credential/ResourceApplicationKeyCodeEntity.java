package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;

@Entity
public class ResourceApplicationKeyCodeEntity {
    private Long id;
    private byte[] code;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NaturalId
    @Column(unique = true)
    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }
}
