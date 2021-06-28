package org.zith.expr.ctxwl.webapi;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.webapi.emailregistration.EmailRegistrationWebCollection;
import org.zith.expr.ctxwl.webapi.mapper.ObjectMapperProvider;

public class CtxwlWebApiApplication extends ResourceConfig {

    private final IdentityServiceSessionFactory identityServiceSessionFactory;

    public CtxwlWebApiApplication(IdentityServiceSessionFactory identityServiceSessionFactory) {
        this.identityServiceSessionFactory = identityServiceSessionFactory;

        property("jersey.config.server.wadl.disableWadl", "true");
        register(ObjectMapperProvider.class);
        register(new RepositoryBinder());

        register(EmailRegistrationWebCollection.class);
    }

    private class RepositoryBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(identityServiceSessionFactory).to(IdentityServiceSessionFactory.class);
        }
    }
}
