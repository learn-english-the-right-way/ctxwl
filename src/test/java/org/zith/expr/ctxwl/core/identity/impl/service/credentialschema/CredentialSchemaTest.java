package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.CredentialManager;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CredentialSchemaTest {
    @Test
    public void testAuthenticationKeyGenerationAndValidation() {
        var repository = CredentialSchemaImpl.create(new Random(), Clock.systemDefaultZone());
        repository.updateKeys(0, new String[]{"TestKey"});
        var code =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key = repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code);
        assertTrue(repository.validateApplicationKey(Set.of(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION), key).isPresent());
        assertEquals(key, repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code));
    }

    @Test
    public void testAuthenticationKeyGenerationAndValidationWithAdditionalMainKeys() {
        var repository = CredentialSchemaImpl.create(new Random(), Clock.systemDefaultZone());
        repository.updateKeys(0, new String[]{"TestKey1"});
        var code1 =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key1 = repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code1);
        repository.updateKeys(0, new String[]{"TestKey1", "TestKey2"});
        assertTrue(repository.validateApplicationKey(Set.of(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION), key1).isPresent());
        var code2 =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0001".getBytes(StandardCharsets.UTF_8);
        var key2 = repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code2);
        repository.updateKeys(1, new String[]{"TestKey2", "TestKey3"});
        assertTrue(repository.validateApplicationKey(Set.of(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION), key2).isPresent());
        assertTrue(repository.validateApplicationKey(Set.of(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION), key1).isEmpty());
        var key3 = repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code2);
        assertNotEquals(key2, key3);
    }

    @Test
    public void testNoKey() {
        var repository = CredentialSchemaImpl.create(new Random(), Clock.systemDefaultZone());
        var code =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key = repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code);
        assertTrue(repository.validateApplicationKey(Set.of(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION), key).isPresent());
        assertEquals(key, repository.makeApplicationKey(CredentialManager.KeyUsage.REGISTRATION_CONFIRMATION, code));
    }
}