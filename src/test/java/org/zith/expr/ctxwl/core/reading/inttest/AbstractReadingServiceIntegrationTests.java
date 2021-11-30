package org.zith.expr.ctxwl.core.reading.inttest;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.NotNull;
import org.zith.expr.ctxwl.common.inttest.AbstractIntegrationTests;
import org.zith.expr.ctxwl.core.reading.InterceptedReadingServiceCreator;
import org.zith.expr.ctxwl.core.reading.ReadingService;
import org.zith.expr.ctxwl.core.reading.inttest.config.ReadingServiceTestConfiguration;
import org.zith.expr.ctxwl.core.reading.inttest.config.ReadingServiceTestConfigurator;

import java.io.File;
import java.util.function.Supplier;

public abstract class AbstractReadingServiceIntegrationTests extends AbstractIntegrationTests<ReadingServiceTestConfiguration> {
    private final Supplier<ReadingService> readingServiceSupplier;

    public AbstractReadingServiceIntegrationTests() {
        readingServiceSupplier = Suppliers.memoize(() -> InterceptedReadingServiceCreator.create(
                configuration().postgreSql().effectiveConfiguration(),
                configuration().mongoDb().effectiveConfiguration()
        ));
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
