package org.zith.expr.ctxwl.core.identity.impl;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.tool.schema.Action;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSession;
import org.zith.expr.ctxwl.core.identity.IdentityServiceSessionFactory;
import org.zith.expr.ctxwl.core.identity.config.MailConfiguration;
import org.zith.expr.ctxwl.core.identity.config.PostgreSqlConfiguration;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceAuthenticationKeyEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourceEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.credential.ResourcePasswordEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.email.EmailEntity;
import org.zith.expr.ctxwl.core.identity.impl.repository.emailregistration.EmailRegistrationEntity;
import org.zith.expr.ctxwl.core.identity.impl.service.credentialschema.CredentialSchema;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailServiceImpl;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.function.Function;
import java.util.regex.Pattern;

public class IdentityServiceSessionFactoryImpl implements IdentityServiceSessionFactory {

    private final DataSource dataSource;
    private final StandardServiceRegistry serviceRegistry;
    private final Metadata metadata;
    private final SessionFactory sessionFactory;
    private final CredentialSchema credentialSchema;
    private final MailService mailService;
    private final Clock clock;

    public IdentityServiceSessionFactoryImpl(
            DataSource dataSource,
            StandardServiceRegistry serviceRegistry,
            Metadata metadata,
            SessionFactory sessionFactory,
            CredentialSchema credentialSchema, MailService mailService,
            Clock clock
    ) {
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
        return IdentityServiceSessionImpl.create(sessionFactory, credentialSchema, mailService, clock);
    }

    public static IdentityServiceSessionFactoryImpl create(
            CredentialSchema credentialSchema,
            Clock clock,
            PostgreSqlConfiguration postgreSqlConfiguration,
            MailConfiguration mailConfiguration
    ) {
        var dataSource = postgreSqlConfiguration.makeDataSource();
        var serviceRegistry =
                new StandardServiceRegistryBuilder()
                        .applySetting(AvailableSettings.DATASOURCE, dataSource)
                        .applySetting(AvailableSettings.HBM2DDL_AUTO, Action.CREATE_DROP)
                        .build();
        var metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(EmailEntity.class)
                .addAnnotatedClass(EmailRegistrationEntity.class)
                .addAnnotatedClass(ResourceEntity.class)
                .addAnnotatedClass(ResourceAuthenticationKeyEntity.class)
                .addAnnotatedClass(ResourcePasswordEntity.class)
                .getMetadataBuilder()
                .applyImplicitNamingStrategy(new IdentityServiceImplicitNamingStrategy())
                .applyPhysicalNamingStrategy(new IdentityServicePhysicalNamingStrategy())
                .build();
        var sessionFactory = metadata.getSessionFactoryBuilder().build();
        var mailService = MailServiceImpl.create(mailConfiguration);
        return new IdentityServiceSessionFactoryImpl(
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

    private static class IdentityServiceImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
        @Override
        protected String transformEntityName(EntityNaming entityNaming) {
            var name = super.transformEntityName(entityNaming);
            if (name.endsWith("Entity")) {
                return name.substring(0, name.length() - 6);
            } else {
                return name;
            }
        }
    }

    private static class IdentityServicePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

        private final Function<String, String> tableNameMapper = makeTableNameMapper();
        private final Function<String, String> columnNameMapper = makeColumnNameMapper();

        private Function<String, String> makeTableNameMapper() {
            return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
        }

        private Function<String, String> makeColumnNameMapper() {
            var baseConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
            var suffixPattern = Pattern.compile("^(.*?)(_?[\\p{Upper}\\p{Digit}]+)?$");
            return name -> {
                var m = suffixPattern.matcher(name);
                if (!m.matches()) throw new IllegalStateException();
                return baseConverter.convert(Strings.nullToEmpty(m.group(1)))
                        + Strings.nullToEmpty(m.group(2)).toLowerCase();
            };
        }

        @Override
        public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
            return convert(tableNameMapper, name, context);
        }

        @Override
        public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
            return convert(columnNameMapper, name, context);
        }

        private Identifier convert(Function<String, String> mapper, Identifier name, JdbcEnvironment context) {
            if (name.isQuoted()) {
                return name;
            } else {
                return context.getIdentifierHelper().toIdentifier(mapper.apply(name.getText()));
            }
        }
    }
}
