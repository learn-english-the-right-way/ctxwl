package org.zith.expr.ctxwl.common.hibernate;

import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;

public class SuffixStripingImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    private final String suffix;

    private SuffixStripingImplicitNamingStrategy(String suffix) {
        this.suffix = suffix;
    }

    @Override
    protected String transformEntityName(EntityNaming entityNaming) {
        var name = super.transformEntityName(entityNaming);
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - 6);
        } else {
            return name;
        }
    }

    public static SuffixStripingImplicitNamingStrategy stripEntitySuffix() {
        return new SuffixStripingImplicitNamingStrategy("Entity");
    }
}
