package org.zith.expr.ctxwl.app;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.zith.expr.ctxwl.app.config.AppConfigurator;
import org.zith.expr.ctxwl.common.close.CombinedAutoCloseable;
import org.zith.expr.ctxwl.core.identity.IdentityServiceCreator;
import org.zith.expr.ctxwl.core.reading.ReadingServiceCreator;
import org.zith.expr.ctxwl.webapi.CtxwlWebApiApplication;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class CtxwlApplication {
    private static final String ARG_CONFIG = "config";
    private static final String ARG_VERBOSE = "verbose";
    private static final String ARG_HELP = "help";

    private final List<String> args;

    private CtxwlApplication(List<String> args) {
        this.args = args;
    }

    public void run() throws Exception {
        var options = new Options();
        options.addOption("c", ARG_CONFIG, true, "configuration file");
        options.addOption("v", ARG_VERBOSE, false, "enable extensive logging");
        options.addOption("h", ARG_HELP, false, "print usage");

        CommandLine cmd;
        var parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args.toArray(String[]::new));
        } catch (ParseException e) {
            System.err.print("Illegal argument: ");
            System.err.print(e.getMessage());
            System.err.println();
            try (var writer = new PrintWriter(System.err)) {
                printUsage(options, writer);
            }
            return;
        }

        var config = cmd.getOptionValue(ARG_CONFIG);
        var verbose = cmd.hasOption(ARG_VERBOSE);
        var help = cmd.hasOption(ARG_HELP);

        if (help) {
            try (var writer = new PrintWriter(System.out)) {
                printUsage(options, writer);
            }
            return;
        }

        var arguments = new Arguments(config, verbose);
        run(arguments);
    }

    private void printUsage(Options options, PrintWriter writer) {
        var formatter = new HelpFormatter();
        formatter.printHelp(writer, formatter.getWidth(), "ctxwl", null, options, 0, 0, null);
    }

    private void run(Arguments arguments) throws Exception {
        if (arguments.verbose()) {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
        }

        var configurator = AppConfigurator.create();
        Optional.ofNullable(arguments.config()).ifPresent(config -> configurator.load(new File(config)));
        var configuration = configurator.configuration();

        Server server;
        try (var closeable = CombinedAutoCloseable.create()) {
            var identityService = closeable.register(
                    IdentityServiceCreator.create(
                            Objects.equals(configuration.core().identity().reinitializeData(), true),
                            configuration.core().identity().postgreSql().effectiveConfiguration(),
                            configuration.core().identity().mail().effectiveConfiguration()));

            var readingService = closeable.register(
                    ReadingServiceCreator.create(
                            Objects.equals(configuration.core().reading().reinitializeData(), true),
                            configuration.core().reading().postgreSql().effectiveConfiguration(),
                            configuration.core().reading().mongoDb().effectiveConfiguration()));

            server = JettyHttpContainerFactory.createServer(
                    URI.create(configuration.webApi().effectiveBaseUri()),
                    new CtxwlWebApiApplication(identityService, readingService));
            server.start();
            var asynchronousClosable = new AtomicReference<CombinedAutoCloseable>();
            var termination = new CompletableFuture<Void>();
            server.addEventListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStopped(LifeCycle event) {
                    Optional.ofNullable(asynchronousClosable.get()).ifPresent(CombinedAutoCloseable::close);
                    termination.complete(null);
                }
            });
            asynchronousClosable.set(closeable.transfer());
        }

        server.join();
    }

    public static CtxwlApplication create(String[] args) {
        return new CtxwlApplication(List.of(args));
    }

    private static record Arguments(String config, boolean verbose) {
    }
}
