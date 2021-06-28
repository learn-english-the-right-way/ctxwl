package org.zith.expr.ctxwl.core.identity.impl.repository.credential;

import com.google.common.base.Preconditions;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.ControlledResource;
import org.zith.expr.ctxwl.core.identity.CredentialRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Random;

public class CredentialRepositoryImpl extends AbstractCredentialRepository implements CredentialRepository {
    private final Session session;
    private final Clock clock;
    private final Random random;

    public CredentialRepositoryImpl(Session session, Clock clock) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(clock);

        this.clock = clock;
        random = new Random();

        this.session = session;
    }

    @Override
    public ControlledResource ensure(ResourceType resourceType, String identifier) {
        var name = makeName(resourceType, identifier);
        return session
                .byNaturalId(ResourceEntity.class)
                .using("name", name)
                .with(LockOptions.READ)
                .loadOptional()
                .map(ResourceEntity::getDelegate)
                .map(r -> r.bind(this))
                .orElseGet(() -> ControlledResourceImpl.create(this, name));
    }

    Session getSession() {
        return session;
    }

    @Override
    public void updateKeys(int offset, String[] keys) {
        super.updateKeys(offset, keys);
    }

    @Override
    public boolean validatePassword(String password) {
        return super.validatePassword(password);
    }

    byte[] makeSalt(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        random.nextBytes(salt);
        return salt;
    }

    Instant timestamp() {
        return clock.instant();
    }

    byte[] makeEntropicCode(int size) {
        Preconditions.checkArgument(size > 0);
        var salt = new byte[size];
        random.nextBytes(salt);
        return salt;
    }
}
