package org.zith.expr.ctxwl.webapi;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.webapi.authentication.EmailRegistrantAuthenticationFilter;
import org.zith.expr.ctxwl.webapi.emailregistration.EmailRegistrationWebCollection;
import org.zith.expr.ctxwl.webapi.mapper.ObjectMapperProvider;

public class CtxwlWebApiApplication extends ResourceConfig {

    private final IdentityService identityService;

    public CtxwlWebApiApplication(IdentityService identityService) {
        this.identityService = identityService;

        property("jersey.config.server.wadl.disableWadl", "true");
        register(ObjectMapperProvider.class);
        register(new RepositoryBinder());

        register(EmailRegistrantAuthenticationFilter.class);

        register(EmailRegistrationWebCollection.class);
    }

    private class RepositoryBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(identityService).to(IdentityService.class);
            bind(identityService).to(IdentityServiceSessionFactory.class);
        }
    }
}
