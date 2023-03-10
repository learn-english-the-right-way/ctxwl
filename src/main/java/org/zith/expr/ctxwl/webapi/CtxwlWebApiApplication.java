package org.zith.expr.ctxwl.webapi;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.zith.expr.ctxwl.core.accesscontrol.AccessPolicy;
import org.zith.expr.ctxwl.core.accesscontrol.Realm;
import org.zith.expr.ctxwl.core.identity.IdentityService;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyAuthenticationException;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyAuthenticationExceptionModule;
import org.zith.expr.ctxwl.webapi.authentication.CtxwlKeyAuthenticationFilter;
import org.zith.expr.ctxwl.webapi.authentication.RealmFactory;
import org.zith.expr.ctxwl.webapi.authorization.AccessPolicyFactory;
import org.zith.expr.ctxwl.webapi.common.WebApiDataException;
import org.zith.expr.ctxwl.webapi.common.WebApiExceptionModule;
import org.zith.expr.ctxwl.webapi.endpoint.authentication.AuthenticationExceptionModule;
import org.zith.expr.ctxwl.webapi.endpoint.authentication.AuthenticationWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.emailregistration.EmailRegistrationExceptionModule;
import org.zith.expr.ctxwl.webapi.endpoint.emailregistration.EmailRegistrationWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry.ReadingHistoryEntryWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.readinghistoryentry.ReadingHistoryExceptionModule;
import org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup.ReadingInspiredLookupExceptionModule;
import org.zith.expr.ctxwl.webapi.endpoint.readinginspiredlookup.ReadingInspiredLookupWebCollection;
import org.zith.expr.ctxwl.webapi.endpoint.readingsession.ReadingSessionExceptionModule;
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
        register(ReadingInspiredLookupWebCollection.class);
        register(ReadingSessionWebCollection.class);

        var injector = Guice.createInjector(
                new WebApiExceptionModule(),
                new CtxwlKeyAuthenticationExceptionModule(),
                new AuthenticationExceptionModule(),
                new EmailRegistrationExceptionModule(),
                new ReadingHistoryExceptionModule(),
                new ReadingInspiredLookupExceptionModule(),
                new ReadingSessionExceptionModule()
        );
        register(injector.getInstance(Key.get(new TypeLiteral<ExceptionMapper<WebApiDataException>>() {
        })));
        register(injector.getInstance(Key.get(new TypeLiteral<ExceptionMapper<CtxwlKeyAuthenticationException>>() {
        })));
    }

    private class RepositoryBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(identityService).to(IdentityService.class).to(IdentityServiceSessionFactory.class);
            bind(readingService).to(ReadingService.class);
            bindFactory(RealmFactory.class).to(Realm.class);
            bindFactory(AccessPolicyFactory.class).to(AccessPolicy.class);
        }
    }
}
