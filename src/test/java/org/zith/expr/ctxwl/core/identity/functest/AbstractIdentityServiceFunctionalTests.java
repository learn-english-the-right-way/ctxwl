package org.zith.expr.ctxwl.core.identity.functest;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.inttest.AbstractFunctionalTests;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.InterceptedIdentityServiceCreator;
import org.zith.expr.ctxwl.core.identity.functest.config.IdentityServiceTestConfiguration;
import org.zith.expr.ctxwl.core.identity.functest.config.IdentityServiceTestConfigurator;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;
import org.zith.expr.ctxwl.webapi.authentication.RealmFactory;

import java.io.File;
import java.util.function.Supplier;

public abstract class AbstractIdentityServiceFunctionalTests extends AbstractFunctionalTests<IdentityServiceTestConfiguration> {
    private final Supplier<IdentityService> identityServiceSupplier;
    private final Supplier<RealmFactory> realmFactorySupplier;
    private final Supplier<Realm> realmSupplier;

    public AbstractIdentityServiceFunctionalTests() {
        identityServiceSupplier = Suppliers.memoize(() ->
                InterceptedIdentityServiceCreator.create(
                        configuration().postgreSql().effectiveConfiguration(),
                        configuration().mail().effectiveConfiguration()));
        realmFactorySupplier = Suppliers.memoize(() -> new RealmFactory(identityService()));
        realmSupplier = Suppliers.memoize(() -> realmFactory().provide());
    }

    protected final IdentityService identityService() {
        return identityServiceSupplier.get();
    }

    protected final RealmFactory realmFactory() {
        return realmFactorySupplier.get();
    }

    protected final Realm realm() {
        return realmSupplier.get();
    }

    @Override
    protected final @NotNull IdentityServiceTestConfiguration parseConfiguration(File... files) {
        var configurator = new IdentityServiceTestConfigurator();
        for (int i = files.length - 1; i >= 0; i--) {
            configurator.load(files[i]);
        }
        return configurator.configuration();
    }
}
