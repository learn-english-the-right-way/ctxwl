package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.generators.BCrypt;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.ControlledResourceUniversalIdentifier;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class ControlledResourceImpl implements ManagedControlledResource {
    private final ResourceEntity entity;

    private CredentialRepositoryImpl repository;

    public ControlledResourceImpl(ResourceEntity entity) {
        this.entity = entity;
    }

    private void initialize(ControlledResourceType resourceType, String identifier) {
        entity.setName(repository.makeName(resourceType, identifier));
        entity.setType(repository.typeName(resourceType));
        entity.setIdentifier(identifier);
        entity.setEntrySerial(1);
        entity.setPasswords(Collections.emptySet());
        entity.setApplicationKeys(Collections.emptySet());
        repository.getSession().persist(entity);
    }

    @Override
    public ControlledResourceType getType() {
        return Arrays.stream(ControlledResourceType.values())
                .filter(t -> Objects.equals(repository.typeName(t), entity.getType()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown type: " + entity.getType()));
    }

    @Override
    public String getIdentifier() {
        return entity.getIdentifier();
    }

    @Override
    public ControlledResourceUniversalIdentifier getUniversalIdentifier() {
        return new ControlledResourceUniversalIdentifier(getType(), getIdentifier());
    }

    @Override
    public boolean validatePassword(CredentialManager.KeyUsage keyUsage, String password) {
        if (!repository.validateStructureOfPassword(password)) {
            return false;
        }

        var optionalPasswordEntity = entity.getPasswords().stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), repository.keyUsageName(keyUsage)))
                .findAny();

        if (optionalPasswordEntity.isEmpty()) {
            return false;
        }

        var passwordEntity = optionalPasswordEntity.get();

        var optionalAlgorithm = Arrays.stream(PasswordAlgorithm.values())
                .filter(a -> Objects.equals(passwordEntity.getAlgorithm(), a.name))
                .findAny();

        if (optionalAlgorithm.isEmpty()) {
            return false;
        }

        var algorithm = optionalAlgorithm.get();

        switch (algorithm) {
            case BCRYPT_12 -> {
                var optionalSalt =
                        Optional.ofNullable(passwordEntity.getSalt()).map(BaseEncoding.base64()::decode);
                var optionalHashedPassword =
                        Optional.ofNullable(passwordEntity.getHashedPassword()).map(BaseEncoding.base64()::decode);
                if (optionalSalt.isEmpty() || optionalHashedPassword.isEmpty()) {
                    return false;
                }
                var salt = optionalSalt.get();
                var hashPassword = optionalHashedPassword.get();
                var expectedHashedPassword = password.getBytes(StandardCharsets.UTF_8);
                return Arrays.equals(hashPassword, BCrypt.generate(expectedHashedPassword, salt, 12));
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public void setPassword(CredentialManager.KeyUsage keyUsage, String password) {
        Preconditions.checkArgument(repository.validateStructureOfPassword(password));

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
    public String ensureApplicationKey(CredentialManager.KeyUsage keyUsage) {
        var optionalExistingApplicationKey = getApplicationKey(keyUsage);
        if (optionalExistingApplicationKey.isPresent()) {
            return optionalExistingApplicationKey.get();
        }

        invalidateKey(keyUsage);

        var applicationKeyEntity = new ResourceApplicationKeyEntity();

        applicationKeyEntity.setResourceId(entity.getId());
        applicationKeyEntity.setResource(entity);
        applicationKeyEntity.setId(entity.getEntrySerial());
        entity.setEntrySerial(entity.getEntrySerial() + 1);

        applicationKeyEntity.setKeyUsage(repository.keyUsageName(keyUsage));

        repository.getSession().flush();
        byte[] code;
        do {
            code = repository.makeEntropicCode(36);
        } while (!checkCodeBeingAvailable(code));
        applicationKeyEntity.setCode(code);

        applicationKeyEntity.setCreation(repository.timestamp());

        var effectiveCodeEntity = new ResourceApplicationKeyCodeEntity();
        effectiveCodeEntity.setCode(code);
        applicationKeyEntity.setEffectiveCode(effectiveCodeEntity);
        repository.getSession().persist(effectiveCodeEntity);

        repository.getSession().persist(applicationKeyEntity);

        this.entity.setApplicationKeys(Stream.concat(
                this.entity.getApplicationKeys().stream(),
                Stream.of(applicationKeyEntity)
        ).toList());

        return repository.makeApplicationKey(keyUsage, code);
    }

    private boolean checkCodeBeingAvailable(byte[] code) {
        var session = repository.getSession();
        var cb = session.getCriteriaBuilder();
        var q = cb.createQuery(Long.class);
        var k = q.from(ResourceApplicationKeyCodeEntity.class);
        q.select(cb.count(k)).where(cb.equal(k.get(ResourceApplicationKeyCodeEntity_.code), code));
        return session.createQuery(q).uniqueResult() <= 0;
    }

    @Override
    public void invalidateKey(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);
        entity.getPasswords()
                .stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .forEach(e -> e.setInvalidation(repository.timestamp()));
        entity.getApplicationKeys()
                .stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .forEach(applicationKeyEntity -> {
                    applicationKeyEntity.setEffectiveCode(null);
                    applicationKeyEntity.setInvalidation(repository.timestamp());
                });
        entity.setPasswords(
                this.entity.getPasswords().stream()
                        .filter(e -> !Objects.equals(e.getKeyUsage(), keyUsageName))
                        .toList());
        entity.setApplicationKeys(
                this.entity.getApplicationKeys().stream()
                        .filter(e -> !Objects.equals(e.getKeyUsage(), keyUsageName))
                        .toList());
    }

    @Override
    public Optional<byte[]> getApplicationKeyCode(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);

        return entity.getApplicationKeys().stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .findAny()
                .map(ResourceApplicationKeyEntity::getCode)
                .map(v -> Arrays.copyOf(v, v.length));
    }

    @Override
    public Optional<String> getApplicationKey(CredentialManager.KeyUsage keyUsage) {
        var keyUsageName = repository.keyUsageName(keyUsage);

        return entity.getApplicationKeys().stream()
                .filter(e -> Objects.equals(e.getKeyUsage(), keyUsageName))
                .findAny()
                .map(e -> repository.makeApplicationKey(keyUsage, e.getCode()));
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
            var optionalSourceAuthorizationKeyEntity = sourceEntity.getApplicationKeys().stream()
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
                var applicationKeyEntity = new ResourceApplicationKeyEntity();

                applicationKeyEntity.setResourceId(entity.getId());
                applicationKeyEntity.setResource(entity);
                applicationKeyEntity.setId(entity.getEntrySerial());
                entity.setEntrySerial(entity.getEntrySerial() + 1);

                applicationKeyEntity.setKeyUsage(repository.keyUsageName(targetKeyUsage));
                applicationKeyEntity.setCode(sourceAuthorizationKeyEntity.getCode());
                applicationKeyEntity.setCreation(sourceAuthorizationKeyEntity.getCreation());
                applicationKeyEntity.setExpiry(sourceAuthorizationKeyEntity.getExpiry());
                applicationKeyEntity.setInvalidation(sourceAuthorizationKeyEntity.getInvalidation());

                var effectiveCodeEntity = new ResourceApplicationKeyCodeEntity();
                effectiveCodeEntity.setCode(applicationKeyEntity.getCode());
                applicationKeyEntity.setEffectiveCode(effectiveCodeEntity);
                repository.getSession().persist(effectiveCodeEntity);

                entity.setApplicationKeys(Stream.concat(
                        entity.getApplicationKeys().stream(),
                        Stream.of(applicationKeyEntity)
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
            ControlledResourceType resourceType,
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
