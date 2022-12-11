package org.zith.expr.ctxwl.webapi.common;

import org.zith.expr.ctxwl.webapi.error.ExceptionExplainerDescriptor;

import java.util.Collection;

public interface WebApiExceptionExplainerRepository {
    Collection<ExceptionExplainerDescriptor> descriptors();
}
