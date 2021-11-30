package org.zith.expr.ctxwl.webapi.access;

import java.util.List;

public interface Principal {
    Realm realm();

    List<Role> roles();

    Subject subject();
}
