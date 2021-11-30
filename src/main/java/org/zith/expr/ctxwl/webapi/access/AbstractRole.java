package org.zith.expr.ctxwl.webapi.access;

import com.google.common.net.UrlEscapers;

abstract class AbstractRole implements Role {
    public static String escape(String name) {
        return UrlEscapers.urlFormParameterEscaper().escape(name);
    }
}
