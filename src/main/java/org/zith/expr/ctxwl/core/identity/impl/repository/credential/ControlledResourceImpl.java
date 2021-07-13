package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.generators.BCrypt;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class ControlledResourceImpl implements ManagedControlledResource {
    private final ResourceEntity entity;

    private CredentialRepositoryImpl repository;

    public ControlledResourceImpl(ResourceEntity entity) {
        this.entity = entity;
    }

    private void initialize(CredentialManager.ResourceType resourceType, String identifier) {
        entity.setName(repository.makeName(resourceType, identifier));
        entity.setType(repository.typeName(resourceType));
        entity.setIdentifier(identifier);
        entity.setEntrySerial(1);
        entity.setPasswords(Collections.emptySet());
        entity.setAuthenticationKeys(Collections.emptySet());
        repository.getSession().persist(entity);
    }

    @Override
    public CredentialManager.ResourceType getType() {
        return Arrays.stream(CredentialManager.ResourceType.values())
                .filter(t -> Objects.equals(repository.typeName(t), entity.getType()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown type: " + entity.getType()));
    }

    @Override
    public String getIdentifier() {
        return entity.getIdentifier();
    }

    @Override
    public void setPassword(CredentialManager.KeyUsage keyUsage, String password) {
        Preconditions.checkArgument(repository.validatePassword(password));

        invalidateKey(keyUsage);

        var passwordEntity = new ResourcePasswordEntity();

        passwordEntity.setResourceId(entity.getId());
        passwordEntity.setResource(entity);
        passwordEntity.setId(entity.getEntrySerial());
        entity.setEntrySerial(entity.getEntrySerial() + 1);

        passwordEntity.setKeyUsage(repository.keyUsageName(keyUsage));

        passwordEntity.setAlgorithm(PasswordAlgorithm.BCRYPT_12.name);
        var salt = repository.makeSalt(16);
        passwordEntity.setSalt(BaseEncoding.base64().encode(salt));
        var hashedPassword = BCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 12);
        passwordEntity.setHashedPassword(BaseEncoding.base64().encode(hashedPassword));

        passwordEntity.setCreation(repository.timestamp());

        repository.getSession().persist(passwordEntity);

        this.entity.setPasswords(
                Stream.concat(this.entity.getPasswords().stream(), Stream.of(passwordEntity)).toList());
    }

    @Override
    public String ensureAuthenticationKey(CredentialManager.KeyUsage keyUsage) {
        var optionalExistingAuthenticationKey = getAuthenticationKey(keyUsage);
        if (optionalExistingAuthenticationKey.isPresent()) {
            return optionalExistingAuthenticationKey.get();
        }

        invalidateKey(keyUsage);

        var authenticationKeyEntity = new ResourceAuthenticationKeyEntity();

        authenticationKeyEntity.setResourceId(entity.getId());
        authenticationKeyEntity.setResource(entity);
        authenticationKeyEntity.setId(entity.getEntrySerial());
        entity.setEntrySerial(entity.getEntrySerial() + 1);

        authenticationKeyEntity.setKeyUsage(repository.keyUsageName(keyUsage));

        byte[] code;
        do {
            code = repository.makeEntropicCode(36);
        } while (!checkCodeBeingAvailable(code));
        authenticationKeyEntity.setCode(code);

        authenticationKeyEntity.setCreation(repository.timestamp());

        var effectiveCodeEntity = new ResourceAuthenticationKeyCodeEntity();
        effectiveCodeEntity.setCode(code);
        authenticationKeyEntity.setEffectiveCode(effectiveCodeEntity);
        repository.getSession().persist(effectiveCodeEntity);

        repository.getSession().persist(authenticationKeyEntity);

        this.entity.setAuthenticationKeys(Stream.concat(
                this.entity.getAuthenticationKeys().stream(),
                Stream.of(authenticationKeyEntity)
        ).toList());

        return repository.makeAuthenticationKey(keyUsage, code);
    }

    private boolean checkCodeBeingAvailable(byte[] code) {
        var session = repository.getSession();
        var cb = session.getCriteriaBuilder();
        var q = cb.createQuery(Long.class);
        var k = q.from(ResourceAuthenticationKeyCodeEntity.class);
        q.select(cb.count(k)).where(cb.equal(k.get(ResourceAuthenticationKeyCodeEntity_.code), code));
        return session.createQuery(q).uniqueResult() <= 0;
    }

    @Override
    public void invalidateKey(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);
        entity.getPasswords()
                .stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .forEach(e -> e.setInvalidation(repository.timestamp()));
        entity.getAuthenticationKeys()
                .stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .forEach(authenticationKeyEntity -> {
                    authenticationKeyEntity.setEffectiveCode(null);
                    authenticationKeyEntity.setInvalidation(repository.timestamp());
                });
        entity.setPasswords(
                this.entity.getPasswords().stream()
                        .filter(e -> !Objects.equals(e.getKeyUsage(), keyUsageName))
                        .toList());
        entity.setAuthenticationKeys(
                this.entity.getAuthenticationKeys().stream()
                        .filter(e -> !Objects.equals(e.getKeyUsage(), keyUsageName))
                        .toList());
    }

    @Override
    public Optional<byte[]> getAuthenticationKeyCode(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);

        return entity.getAuthenticationKeys().stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .findAny()
                .map(ResourceAuthenticationKeyEntity::getCode)
                .map(v -> Arrays.copyOf(v, v.length));
    }

    @Override
    public Optional<String> getAuthenticationKey(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);

        return entity.getAuthenticationKeys().stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .findAny()
                .map(e -> repository.makeAuthenticationKey(keyUsage, e.getCode()));
    }

    @Override
    public void importKey(
            ControlledResource source,
            CredentialManager.KeyUsage sourceKeyUsage,
            CredentialManager.KeyUsage targetKeyUsage,
            boolean migrate
    ) {
        if (source instanceof ManagedControlledResource managedSource) {
            var sourceEntity = managedSource.getEntity();
            var optionalSourcePasswordEntity = sourceEntity.getPasswords().stream()
                    .filter(e -> Objects.equals(e.getKeyUsage(), repository.keyUsageName(sourceKeyUsage)))
                    .findAny();
            var optionalSourceAuthorizationKeyEntity = sourceEntity.getAuthenticationKeys().stream()
                    .filter(e -> Objects.equals(e.getKeyUsage(), repository.keyUsageName(sourceKeyUsage)))
                    .findAny();

            if (optionalSourcePasswordEntity.isEmpty() && optionalSourceAuthorizationKeyEntity.isEmpty()) {
                throw new NoSuchElementException();
            }

            if (optionalSourceAuthorizationKeyEntity.isPresent() && !migrate) {
                throw new IllegalArgumentException("Authorization keys cannot be imported without migration");
            }

            invalidateKey(targetKeyUsage);

            if (migrate) {
                managedSource.invalidateKey(sourceKeyUsage);
            }

            optionalSourcePasswordEntity.ifPresent(sourcePasswordEntity -> {
                var passwordEntity = new ResourcePasswordEntity();

                passwordEntity.setResourceId(entity.getId());
                passwordEntity.setResource(entity);
                passwordEntity.setId(entity.getEntrySerial());
                entity.setEntrySerial(entity.getEntrySerial() + 1);

                passwordEntity.setKeyUsage(repository.keyUsageName(targetKeyUsage));
                passwordEntity.setAlgorithm(sourcePasswordEntity.getAlgorithm());
                passwordEntity.setSalt(sourcePasswordEntity.getSalt());
                passwordEntity.setHashedPassword(sourcePasswordEntity.getHashedPassword());
                passwordEntity.setCreation(sourcePasswordEntity.getCreation());
                passwordEntity.setExpiry(sourcePasswordEntity.getExpiry());
                passwordEntity.setInvalidation(sourcePasswordEntity.getInvalidation());

                repository.getSession().persist(passwordEntity);

                entity.setPasswords(
                        Stream.concat(entity.getPasswords().stream(), Stream.of(passwordEntity)).toList());
            });

            optionalSourceAuthorizationKeyEntity.ifPresent(sourceAuthorizationKeyEntity -> {
                var authenticationKeyEntity = new ResourceAuthenticationKeyEntity();

                authenticationKeyEntity.setResourceId(entity.getId());
                authenticationKeyEntity.setResource(entity);
                authenticationKeyEntity.setId(entity.getEntrySerial());
                entity.setEntrySerial(entity.getEntrySerial() + 1);

                authenticationKeyEntity.setKeyUsage(repository.keyUsageName(targetKeyUsage));
                authenticationKeyEntity.setCode(sourceAuthorizationKeyEntity.getCode());
                authenticationKeyEntity.setCreation(sourceAuthorizationKeyEntity.getCreation());
                authenticationKeyEntity.setExpiry(sourceAuthorizationKeyEntity.getExpiry());
                authenticationKeyEntity.setInvalidation(sourceAuthorizationKeyEntity.getInvalidation());

                var effectiveCodeEntity = new ResourceAuthenticationKeyCodeEntity();
                effectiveCodeEntity.setCode(authenticationKeyEntity.getCode());
                authenticationKeyEntity.setEffectiveCode(effectiveCodeEntity);
                repository.getSession().persist(effectiveCodeEntity);

                entity.setAuthenticationKeys(Stream.concat(
                        entity.getAuthenticationKeys().stream(),
                        Stream.of(authenticationKeyEntity)
                ).toList());
            });
        } else {
            throw new IllegalArgumentException(
                    "This controlled resource cannot import keys from a foreign controlled resource");
        }
    }

    public ControlledResourceImpl bind(CredentialRepositoryImpl repository) {
        this.repository = repository;
        return this;
    }

    public static ControlledResourceImpl create(
            CredentialRepositoryImpl repository,
            CredentialManager.ResourceType resourceType,
            String identifier
    ) {
        var controlledResource = new ResourceEntity().getDelegate().bind(repository);
        controlledResource.initialize(resourceType, identifier);
        return controlledResource;
    }

    @Override
    public ResourceEntity getEntity() {
        return entity;
    }

    private enum PasswordAlgorithm {
        BCRYPT_12("bcrypt/12");

        private final String name;

        PasswordAlgorithm(String name) {
            this.name = name;
        }
    }
}
