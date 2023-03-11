package org.zith.expr.ctxwl.webapi.authorization.role;

import org.zith.expr.ctxwl.core.accesscontrol.Role;

public record EmailRegistrantRole(String emailRegistrationIdentifier) implements Role {
}
