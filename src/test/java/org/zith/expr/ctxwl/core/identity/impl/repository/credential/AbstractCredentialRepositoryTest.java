package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import org.junit.jupiter.api.Test;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AbstractCredentialRepositoryTest {
    @Test
    public void testAuthenticationKeyGenerationAndValidation() {
        var repository = new TestableCredentialRepository();
        repository.updateKeys(0, new String[]{"TestKey"});
        var code =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key = repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code);
        assertTrue(repository.validateAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, key));
        assertEquals(key, repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code));
    }

    @Test
    public void testAuthenticationKeyGenerationAndValidationWithAdditionalMainKeys() {
        var repository = new TestableCredentialRepository();
        repository.updateKeys(0, new String[]{"TestKey1"});
        var code1 =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key1 = repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code1);
        repository.updateKeys(0, new String[]{"TestKey1", "TestKey2"});
        assertTrue(repository.validateAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, key1));
        var code2 =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0001".getBytes(StandardCharsets.UTF_8);
        var key2 = repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code2);
        repository.updateKeys(1, new String[]{"TestKey2", "TestKey3"});
        assertTrue(repository.validateAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, key2));
        assertFalse(repository.validateAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, key1));
        var key3 = repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code2);
        assertNotEquals(key2, key3);
    }

    @Test
    public void testNoKey() {
        var repository = new TestableCredentialRepository();
        var code =
                "TestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCodeTestCode0000".getBytes(StandardCharsets.UTF_8);
        var key = repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code);
        assertTrue(repository.validateAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, key));
        assertEquals(key, repository.makeAuthenticationKey(CredentialRepository.KeyUsage.REGISTRATION_CONFIRMATION, code));
    }

    private static class TestableCredentialRepository extends AbstractCredentialRepository {
    }
}