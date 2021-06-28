package org.zith.expr.ctxwl.core.identity;

public interface EmailRegistration {
    String getConfirmationCode();

    Email getEmail();

    ControlledResource getControlledResource();
}
