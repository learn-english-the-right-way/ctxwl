package org.zith.expr.ctxwl.core.identity.impl.service.credentialschema;

import org.zith.expr.ctxwl.core.identity.ControlledResourceType;

public record ControlledResourceName(ControlledResourceType type, String identifier) {
}