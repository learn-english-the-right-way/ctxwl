package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import org.zith.expr.ctxwl.core.identity.CredentialManager;

public record ControlledResourceName(CredentialManager.ResourceType type, String identifier) {
}