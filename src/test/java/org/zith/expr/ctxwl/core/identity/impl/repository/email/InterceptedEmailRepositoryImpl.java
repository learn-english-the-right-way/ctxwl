package org.zith.expr.ctxwl.core.identity.impl.repository.email;

import org.hibernate.Session;
import org.zith.expr.ctxwl.core.identity.UserRepository;
import org.zith.expr.ctxwl.core.identity.impl.ComponentFactory;
import org.zith.expr.ctxwl.core.identity.impl.service.mail.MailService;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InterceptedEmailRepositoryImpl extends EmailRepositoryImpl {
    private final ConcurrentLinkedQueue<Interceptor> interceptors;

    public InterceptedEmailRepositoryImpl(
            ComponentFactory componentFactory,
            Session session,
            MailService mailService,
            UserRepository userRepository
    ) {
        super(componentFactory, session, mailService, userRepository);
        interceptors = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void interceptInsertion(EmailEntity freshEntity) {
        interceptors.forEach(i -> i.interceptInsertion(freshEntity));
        super.interceptInsertion(freshEntity);
    }

    Optional<Interceptor.Cancellation> inject(Interceptor interceptor) {
        interceptors.add(interceptor);
        return Optional.of(new SimpleCancellation(interceptor));
    }

    private class SimpleCancellation implements Interceptor.Cancellation {
        private final Interceptor interceptor;

        public SimpleCancellation(Interceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void cancel() {
            interceptors.remove(interceptor);
        }
    }

    interface Interceptor {
        void interceptInsertion(EmailEntity freshEntity);

        interface Cancellation {
            void cancel();
        }
    }
}
