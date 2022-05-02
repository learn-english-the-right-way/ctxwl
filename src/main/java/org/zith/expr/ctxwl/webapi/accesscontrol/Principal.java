package org.zith.expr.ctxwl.webapi.accesscontrol;

import java.util.List;

public interface Principal {
    Realm realm();

    List<Role> roles();

    Subject subject();
}
