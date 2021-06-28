package org.zith.expr.ctxwl;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.zith.expr.ctxwl.app.CtxwlApplication;

public final class Ctxwl {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) {
        var logger = LoggerFactory.getLogger(Ctxwl.class);
        try {
            var application = CtxwlApplication.create(args);
            application.run();
        } catch (Throwable th) {
            logger.error("Unhandled error", th);
        }
    }
}
