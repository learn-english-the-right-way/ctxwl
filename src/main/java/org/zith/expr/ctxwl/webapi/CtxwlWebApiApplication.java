package org.zith.expr.ctxwl.webapi;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.access.Realm;
import org.zith.expr.ctxwl.webapi.access.RealmFactory;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyAuthenticationFilter;
import org.zith.expr.ctxwl.webapi.endpoint.authentication.AuthenticationWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.emailregistration.EmailRegistrationWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry.ReadingHistoryEntryWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.readingsession.ReadingSessionWebCollection;
import org.zith.expr.ctxwl.webapi.mapper.ObjectMapperProvider;

public class CtxwlWebApiApplication extends ResourceConfig {
    private final IdentityService identityService;
    private final ReadingService readingService;

    public CtxwlWebApiApplication(IdentityService identityService, ReadingService readingService) {
        this.identityService = identityService;
        this.readingService = readingService;

        register(new RepositoryBinder());

        property("jersey.config.server.wadl.disableWadl", "true");
        register(ObjectMapperProvider.class);
        register(CtxwlKeyAuthenticationFilter.class);

        register(AuthenticationWebCollection.class);
        register(EmailRegistrationWebCollection.class);
        register(ReadingHistoryEntryWebCollection.class);
        register(ReadingSessionWebCollection.class);
    }

    private class RepositoryBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(identityService).to(IdentityService.class).to(IdentityServiceSessionFactory.class);
            bind(readingService).to(ReadingService.class);
            bindFactory(RealmFactory.class).to(Realm.class);
        }
    }
}
