package org.zith.expr.ctxwl.core.identity.impl;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.zith.expr.ctxwl.common.hibernate.LowerUnderscorePhysicalNamingStrategy;
import org.zith.expr.ctxwl.common.hibernate.SuffixStripingImplicitNamingStrategy;
import org.zith.expr.ctxwl.common.postgresql.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSession;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceApplicationKeyCodeEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceApplicationKeyEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourcePasswordEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration.EmailRegistrationEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.user.UserEntity;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailServiceImpl;

import javax.sql.DataSource;
import java.time.Clock;

public class IdentityServiceSessionFactoryImpl implements IdentityServiceSessionFactory {

    private final ComponentFactory componentFactory;

    private final DataSource dataSource;
    private final StandardServiceRegistry serviceRegistry;
    private final Metadata metadata;
    private final SessionFactory sessionFactory;
    private final CredentialSchema credentialSchema;
    private final MailService mailService;
    private final Clock clock;

    public IdentityServiceSessionFactoryImpl(
            ComponentFactory componentFactory,
            DataSource dataSource,
            StandardServiceRegistry serviceRegistry,
            Metadata metadata,
            SessionFactory sessionFactory,
            CredentialSchema credentialSchema,
            MailService mailService,
            Clock clock
    ) {
        this.componentFactory = componentFactory;
        this.dataSource = dataSource;
        this.serviceRegistry = serviceRegistry;
        this.metadata = metadata;
        this.sessionFactory = sessionFactory;
        this.credentialSchema = credentialSchema;
        this.mailService = mailService;
        this.clock = clock;
    }

    @Override
    public IdentityServiceSession openSession() {
        return componentFactory.createIdentityServiceSessionImpl(sessionFactory, credentialSchema, mailService, clock);
    }

    public static IdentityServiceSessionFactoryImpl create(
            ComponentFactory componentFactory,
            CredentialSchema credentialSchema,
            Clock clock,
            boolean reinitializeData,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        var dataSource =
                postgreSqlConfiguration
                        .makeDataSource(PostgreSqlConfiguration.TransactionIsolation.TRANSACTION_SERIALIZABLE);
        var serviceRegistryBuilder = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DATASOURCE, dataSource)
                .applySetting(AvailableSettings.KEYWORD_AUTO_QUOTING_ENABLED, true);
        if (reinitializeData) {
            serviceRegistryBuilder.applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        }
        var serviceRegistry = serviceRegistryBuilder.build();
        var metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(UserEntity.class)
                .addAnnotatedClass(EmailEntity.class)
                .addAnnotatedClass(EmailRegistrationEntity.class)
                .addAnnotatedClass(ResourceEntity.class)
                .addAnnotatedClass(ResourceApplicationKeyEntity.class)
                .addAnnotatedClass(ResourceApplicationKeyCodeEntity.class)
                .addAnnotatedClass(ResourcePasswordEntity.class)
                .getMetadataBuilder()
                .applyImplicitNamingStrategy(SuffixStripingImplicitNamingStrategy.stripEntitySuffix())
                .applyPhysicalNamingStrategy(new LowerUnderscorePhysicalNamingStrategy())
                .build();
        var sessionFactory = metadata.getSessionFactoryBuilder().build();
        var mailService = MailServiceImpl.create(mailConfiguration);
        return new IdentityServiceSessionFactoryImpl(
                componentFactory,
                dataSource,
                serviceRegistry,
                metadata,
                sessionFactory,
                credentialSchema,
                mailService,
                clock
        );
    }

    @Override
    public void close() {
        sessionFactory.close();
        serviceRegistry.close();
    }

}
