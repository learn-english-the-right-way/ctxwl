package org.zith.expr.ctxwl.core.reading.functest;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.inttest.AbstractFunctionalTests;
import org.zith.expr.ctxwl.core.reading.InterceptedReadingServiceCreator;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.functest.config.ReadingServiceTestConfiguration;
import org.zith.expr.ctxwl.core.reading.functest.config.ReadingServiceTestConfigurator;

import java.io.File;
import java.util.function.Supplier;

public abstract class AbstractReadingServiceFunctionalTests extends AbstractFunctionalTests<ReadingServiceTestConfiguration> {
    private final Supplier<ReadingService> readingServiceSupplier;

    public AbstractReadingServiceFunctionalTests() {
        readingServiceSupplier = Suppliers.memoize(() -> combinedAutoCloseable.register(
                InterceptedReadingServiceCreator.create(
                        configuration().postgreSql().effectiveConfiguration(),
                        configuration().mongoDb().effectiveConfiguration()
                )));
    }

    protected final ReadingService readingService() {
        return readingServiceSupplier.get();
    }

    @Override
    protected final @NotNull ReadingServiceTestConfiguration parseConfiguration(File... files) {
        var configurator = new ReadingServiceTestConfigurator();
        for (int i = files.length - 1; i >= 0; i--) {
            configurator.load(files[i]);
        }
        return configurator.configuration();
    }
}
