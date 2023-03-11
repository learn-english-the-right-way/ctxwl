package org.zith.expr.ctxwl.core.accesscontrol;

import org.zith.expr.ctxwl.core.identity.ControlledResourceUniversalIdentifier;

import java.util.Set;

public interface Principal {
    Realm realm();

    String name();

    ControlledResourceUniversalIdentifier resourceIdentifier();

    Set<String> applicationKeys();
}
