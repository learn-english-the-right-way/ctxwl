package org.zith.expr.ctxwl.common.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DataAccessor<T> {
    protected abstract T operate(Session session);

    public final T execute(Session session) {
        RetryState retryState = RetryState.fresh();
        for (; ; ) {
            var transaction = session.beginTransaction();
            try {
                var result = operate(session);
                if (transaction.getRollbackOnly()) {
                    throw new IllegalStateException();
                }
                transaction.commit();
                return result;
            } catch (Exception e) {
                try {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                } catch (Exception ex) {
                    e.addSuppressed(ex);
                }

                if (!(retryState = merge(retryState, e)).retryable()) {
                    throw e;
                }
            }
        }
    }

    public final T execute(SessionFactory sessionFactory) {
        var session = sessionFactory.openSession();
        T result;
        try {
            result = execute(session);
        } catch (Exception e) {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (Exception ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
        if (session.isOpen()) {
            session.close();
        }
        return result;
    }

    protected RetryState merge(RetryState retryState, Exception e) {
        return retryState;
    }

    private static final class RetryStateFreshStateHolder {
        private static final RetryState INSTANCE = () -> false;
    }

    private static final class RetryStateNeverRetryStateHolder {
        private static final RetryState INSTANCE = () -> false;
    }

    public interface RetryState {

        static RetryState fresh() {
            return RetryStateFreshStateHolder.INSTANCE;
        }

        static RetryState never() {
            return RetryStateNeverRetryStateHolder.INSTANCE;
        }

        boolean retryable();
    }

    public static <T> DataAccessor<T> of(
            Function<Session, T> operation,
            Predicate<Exception> retryableExceptionTester,
            int numberOfRetries
    ) {
        record RetryCount(int n) implements RetryState {
            @Override
            public boolean retryable() {
                return true;
            }
        }
        return new DataAccessor<>() {
            @Override
            protected T operate(Session session) {
                return operation.apply(session);
            }

            @Override
            protected RetryState merge(RetryState retryState, Exception e) {
                if (retryableExceptionTester.test(e)) {
                    if (retryState instanceof RetryCount retryCount) {
                        if (retryCount.n() >= numberOfRetries) {
                            return RetryState.never();
                        } else {
                            return new RetryCount(retryCount.n() + 1);
                        }
                    } else if (retryState == RetryState.fresh()) {
                        if (numberOfRetries >= 1) {
                            return new RetryCount(1);
                        } else {
                            return RetryState.never();
                        }
                    }
                }
                return RetryState.never();
            }
        };
    }

    public interface Factory {
        <T> DataAccessor<T> create(Function<Session, T> operation);

        static Factory of(Predicate<Exception> retryableExceptionTester, int numberOfRetries) {
            return new Factory() {
                @Override
                public <T> DataAccessor<T> create(Function<Session, T> operation) {
                    return DataAccessor.of(operation, retryableExceptionTester, numberOfRetries);
                }
            };
        }
    }
}
